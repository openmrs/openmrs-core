/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

/**
 * The service can be used to convert data from OutputStream to InputStream without copying all data in memory.
 * <p>
 * The {@link #streamData(StreamDataWriter, Long)} method may run {@link StreamDataWriter#write(OutputStream)} in a 
 * separate thread using {@link TaskExecutor}.
 * <p>
 * It's providing the {@link java.io.PipedInputStream}/{@link java.io.PipedOutputStream} mechanism in a thread safe way 
 * with the use of {@link BlockingQueue}.
 * 
 * @since 2.8.0, 2.7.5, 2.6.16, 2.5.15
 */
@Service
public class StreamDataService {
	public static final int BUFFER_SIZE = (int) DataSize.ofKilobytes(128).toBytes();
	
	private static final Logger log = LoggerFactory.getLogger(StreamDataService.class);
	private final TaskExecutor taskExecutor;
	
	public StreamDataService(@Autowired TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	private static class QueueInputStream extends InputStream {
		private final BlockingQueue<Integer> blockingQueue;
		private final long timeoutNanos;
		private volatile IOException streamException;

		public QueueInputStream() {
			this.blockingQueue = new LinkedBlockingQueue<>(BUFFER_SIZE);
			this.timeoutNanos = Duration.ofSeconds(30).toNanos();
		}

		public QueueOutputStream newQueueOutputStream() {
			return new QueueOutputStream(this);
		}

		@Override
		public int read() throws IOException {
			try {
				checkStreamException();
				
				int result;
				Integer peek = this.blockingQueue.peek();
				if (Integer.valueOf(-1).equals(peek)) {
					result = -1;
				} else {
					Integer value = this.blockingQueue.poll(this.timeoutNanos, TimeUnit.NANOSECONDS);
					if (value == null) {
						// Timeout
						result = -1;
					} else if (value == -1) {
						// End of stream. Put the end of stream back in the queue for consistency.
						this.blockingQueue.clear();
						if (!this.blockingQueue.offer(-1, timeoutNanos, TimeUnit.NANOSECONDS)) {
							throw new IOException("Failed to write to full queue");
						}
						result = -1;
					} else {
						result = 255 & value;
					}
				}

				checkStreamException();
				return result;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				InterruptedIOException interruptedIoException = new InterruptedIOException();
				interruptedIoException.initCause(e);
				throw interruptedIoException;
			}
		}

		@Override
		public void close() throws IOException {
			checkStreamException();
			super.close();
		}

		/**
		 * Propagate exception from a writing thread to a reading thread so that processing is stopped.
		 * 
		 * @param streamException exception
		 * @throws UncheckedIOException rethrows e
		 */
		public void propagateStreamException(IOException streamException) {
			this.streamException = streamException;
		}
		
		public void checkStreamException() throws IOException {
			if (streamException != null) {
				throw streamException;
			}
		}
	}
	
	private static class QueueOutputStream extends OutputStream {
		private final QueueInputStream queueInputStream;
		
		public QueueOutputStream(QueueInputStream queueInputStream) {
			this.queueInputStream = queueInputStream;
		}

		/**
		 * @param b   the <code>byte</code>.
		 * @throws IOException when queue full or interrupted
		 */
		@Override
		public void write(int b) throws IOException {
			try {
				queueInputStream.checkStreamException();
				
				if (!queueInputStream.blockingQueue.offer(255 & b, queueInputStream.timeoutNanos, TimeUnit.NANOSECONDS)) {
					IOException streamException = new IOException("Failed to write to full queue");
					queueInputStream.propagateStreamException(streamException);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				InterruptedIOException interruptedIoException = new InterruptedIOException();
				interruptedIoException.initCause(e);
				throw interruptedIoException;
			}
		}

		/**
		 * Closing the stream doesn't fail any following writes, but effectively only data up to closing the stream
		 * is read.
		 * 
		 * @throws IOException when queue full or interrupted
		 */
		@Override
		public void close() throws IOException {
			try {
				queueInputStream.checkStreamException();
				
				// Indicate the end of stream
				if (!this.queueInputStream.blockingQueue.offer(-1, queueInputStream.timeoutNanos, TimeUnit.NANOSECONDS)) {
					IOException streamException = new IOException("Failed to write to full queue");
					queueInputStream.propagateStreamException(streamException);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				InterruptedIOException interruptedIoException = new InterruptedIOException();
				interruptedIoException.initCause(e);
				throw interruptedIoException;
			}
		}
	}

	/**
	 * Runs {@link StreamDataWriter#write(OutputStream)} in a separate thread using {@link TaskExecutor} or copies 
	 * in-memory if the length is smaller than {@link #BUFFER_SIZE}.
	 * <p>
	 * The returned InputStream doesn't need to be closed and the close operation takes no effect.
	 * 
	 * @param writer the write method
	 * @param length the number of bytes if known or null
	 * @return InputStream
	 * 
	 * @throws IOException when failing to stream data
	 */
	public InputStream streamData(StreamDataWriter writer, Long length) throws IOException {
		if (length != null && length < BUFFER_SIZE) {
			ByteArrayOutputStream out = new ByteArrayOutputStream(length.intValue());
			try {
				writer.write(out);
			} catch (Exception e) {
				throw new IOException("Failed to write data to byte array", e);
			}
			return new ByteArrayInputStream(out.toByteArray());
		} else {
			QueueInputStream in = new QueueInputStream();

			taskExecutor.execute(() -> {
				QueueOutputStream out = in.newQueueOutputStream();
				try {
					writer.write(out);
				} catch (Exception e) {
					log.error("Failed to write data in parallel", e);
					in.propagateStreamException(new IOException("Failed to write data in parallel", e));
				} finally {
					// Closing quietly as any exceptions in QueueOutputStream.close() are propagated
					IOUtils.closeQuietly(out);
				}
			});

			return in;
		}
	}
}
