/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.hl7.db.hibernate;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.hl7.HL7Constants;
import org.openmrs.hl7.HL7InArchive;
import org.openmrs.hl7.HL7InError;
import org.openmrs.hl7.HL7InQueue;
import org.openmrs.hl7.HL7Source;
import org.openmrs.hl7.HL7Util;
import org.openmrs.hl7.Hl7InArchivesMigrateThread;
import org.openmrs.hl7.Hl7InArchivesMigrateThread.Status;
import org.openmrs.hl7.db.HL7DAO;

/**
 * OpenMRS HL7 API database default hibernate implementation This class shouldn't be instantiated by
 * itself. Use the {@link org.openmrs.api.context.Context}
 * 
 * @see org.openmrs.hl7.HL7Service
 * @see org.openmrs.hl7.db.HL7DAO
 */
public class HibernateHL7DAO implements HL7DAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateHL7DAO() {
	}
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public HL7Source saveHL7Source(HL7Source hl7Source) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(hl7Source);
		return hl7Source;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7Source(java.lang.Integer)
	 */
	public HL7Source getHL7Source(Integer hl7SourceId) throws DAOException {
		return (HL7Source) sessionFactory.getCurrentSession().get(HL7Source.class, hl7SourceId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7SourceByName(java.lang.String)
	 */
	public HL7Source getHL7SourceByName(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(HL7Source.class);
		crit.add(Restrictions.eq("name", name));
		return (HL7Source) crit.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7Sources()
	 */
	@SuppressWarnings("unchecked")
	public List<HL7Source> getAllHL7Sources() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7Source").list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7Source(org.openmrs.hl7.HL7Source)
	 */
	public void deleteHL7Source(HL7Source hl7Source) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7Source);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public HL7InQueue saveHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(hl7InQueue);
		return hl7InQueue;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueue(java.lang.Integer)
	 */
	public HL7InQueue getHL7InQueue(Integer hl7InQueueId) throws DAOException {
		return (HL7InQueue) sessionFactory.getCurrentSession().get(HL7InQueue.class, hl7InQueueId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InQueues()
	 */
	@SuppressWarnings("unchecked")
	public List<HL7InQueue> getAllHL7InQueues() throws DAOException {
		return sessionFactory.getCurrentSession()
		        .createQuery("from HL7InQueue where messageState = ? order by HL7InQueueId").setParameter(0,
		            HL7Constants.HL7_STATUS_PENDING, Hibernate.INTEGER).list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getNextHL7InQueue()
	 */
	public HL7InQueue getNextHL7InQueue() throws DAOException {
		Query query = sessionFactory.getCurrentSession().createQuery(
		    "from HL7InQueue as hiq where hiq.messageState = ? order by HL7InQueueId").setParameter(0,
		    HL7Constants.HL7_STATUS_PENDING, Hibernate.INTEGER).setMaxResults(1);
		if (query == null)
			return null;
		return (HL7InQueue) query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InQueue(org.openmrs.hl7.HL7InQueue)
	 */
	public void deleteHL7InQueue(HL7InQueue hl7InQueue) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InQueue);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public HL7InArchive saveHL7InArchive(HL7InArchive hl7InArchive) throws DAOException {
		sessionFactory.getCurrentSession().save(hl7InArchive);
		
		return hl7InArchive;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchive(java.lang.Integer)
	 */
	public HL7InArchive getHL7InArchive(Integer hl7InArchiveId) throws DAOException {
		return (HL7InArchive) sessionFactory.getCurrentSession().get(HL7InArchive.class, hl7InArchiveId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchiveByState(Integer stateId)
	 */
	public List<HL7InArchive> getHL7InArchiveByState(Integer state) throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InArchive where messageState = ?").setParameter(0,
		    state, Hibernate.INTEGER).list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InQueueByState(Integer stateId)
	 */
	@SuppressWarnings("unchecked")
	public List<HL7InQueue> getHL7InQueueByState(Integer state) throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InQueue where messageState = ?").setParameter(0,
		    state, Hibernate.INTEGER).list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InArchives()
	 */
	@SuppressWarnings("unchecked")
	public List<HL7InArchive> getAllHL7InArchives() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InArchive order by HL7InArchiveId").list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InArchive(org.openmrs.hl7.HL7InArchive)
	 */
	public void deleteHL7InArchive(HL7InArchive hl7InArchive) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InArchive);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InError(HL7InError)
	 */
	public HL7InError saveHL7InError(HL7InError hl7InError) throws DAOException {
		sessionFactory.getCurrentSession().save(hl7InError);
		return hl7InError;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InError(Integer)
	 */
	public HL7InError getHL7InError(Integer hl7InErrorId) throws DAOException {
		return (HL7InError) sessionFactory.getCurrentSession().get(HL7InError.class, hl7InErrorId);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InErrors()
	 */
	@SuppressWarnings("unchecked")
	public List<HL7InError> getAllHL7InErrors() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from HL7InError order by HL7InErrorId").list();
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#deleteHL7InError(HL7InError)
	 */
	public void deleteHL7InError(HL7InError hl7InError) throws DAOException {
		sessionFactory.getCurrentSession().delete(hl7InError);
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#garbageCollect()
	 */
	public void garbageCollect() {
		Context.clearSession();
	}
	
	/**
	 * @see HL7DAO#migrateHl7InArchivesToFileSystem(Map)
	 */
	public void migrateHl7InArchivesToFileSystem(Map<String, Integer> progressStatusMap) throws DAOException {
		int numberTransferred = 0;
		int numberOfFailedTransfers = 0;
		List<HL7InArchive> hl7InArchives = getAllHL7InArchives(HL7Constants.MIGRATION_MAX_BATCH_SIZE);
		
		//While we still we have any archives in the database, fetch the next batch
		while (Hl7InArchivesMigrateThread.getTransferStatus() == Status.RUNNING && hl7InArchives != null
		        && hl7InArchives.size() > 0) {
			Iterator<HL7InArchive> iterator = hl7InArchives.iterator();
			//if user hasn't yet stopped the migration, e.g clicked stop button in the browser for the web app
			while (Hl7InArchivesMigrateThread.getTransferStatus() == Status.RUNNING && iterator.hasNext()) {
				HL7InArchive archive = iterator.next();
				if (archive.getMessageState().equals(HL7Constants.HL7_STATUS_PROCESSED)) {
					//if the hl7 was successfully written, delete it
					if (writeHL7InArchiveToFileSystem(archive)) {
						deleteHL7InArchive(archive);
						progressStatusMap.put(HL7Constants.NUMBER_TRANSFERRED_KEY, numberTransferred++);
					} else
						progressStatusMap.put(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY, numberOfFailedTransfers++);
				} else {
					//move the message back to the hl7 in archive table
					HL7InQueue archiveToMoveToHl7Queue = new HL7InQueue();
					archiveToMoveToHl7Queue.setHL7Source(archive.getHL7Source());
					archiveToMoveToHl7Queue.setHL7SourceKey(archive.getHL7SourceKey());
					archiveToMoveToHl7Queue.setHL7Data(archive.getHL7Data());
					//maintain the status
					archiveToMoveToHl7Queue.setMessageState(archive.getMessageState());
					archiveToMoveToHl7Queue.setUuid(UUID.randomUUID().toString());
					archiveToMoveToHl7Queue.setDateCreated(archive.getDateCreated());
					
					//if the archive is sent back into the queue, delete it from the archive table
					if (saveHL7InQueue(archiveToMoveToHl7Queue) != null) {
						deleteHL7InArchive(archive);
						progressStatusMap.put(HL7Constants.NUMBER_TRANSFERRED_KEY, numberTransferred++);
						if (log.isDebugEnabled())
							log.debug("Moved hl7 archive with id '" + archive.getHL7InArchiveId()
							        + "' back into the hl7 queue");
					} else
						progressStatusMap.put(HL7Constants.NUMBER_OF_FAILED_TRANSFERS_KEY, numberOfFailedTransfers++);
				}
				
			}
			
			//fetch more archives
			hl7InArchives = getAllHL7InArchives(HL7Constants.MIGRATION_MAX_BATCH_SIZE);
		}
		
		//if user didn't stop the process and all archives have been transferred
		if (Hl7InArchivesMigrateThread.getTransferStatus() != Status.STOPPED && !isArchiveMigrationRequired()) {
			//drop the archives table
			Connection conn = sessionFactory.getCurrentSession().connection();
			Statement stmt = null;
			// check if the tables exist and contains rows.  If not, this has been run before
			try {
				stmt = conn.createStatement();
				stmt.executeUpdate("DROP TABLE hl7_in_archive");
				if (log.isDebugEnabled())
					log.debug("dropped table 'hl7_in_archive'");
				
			}
			catch (SQLException e) {
				log.warn("Failed to drop 'hl7_in_archive' table");
			}
			finally {
				if (stmt != null) {
					try {
						stmt.close();
					}
					catch (SQLException e) {
						log.warn("Error generated", e);
					}
				}
			}
		}
		
		if (log.isDebugEnabled())
			log.debug("Transfer of HL7 archives has completed or has been stopped");
		
	}
	
	/**
	 * @see HL7DAO#getHL7InArchiveInFileSystem(Integer)
	 */
	public HL7InArchive getHL7InArchiveInFileSystem(String uuid) throws DAOException {
		
		//locate the corresponding archive file from the file system
		File hl7InArchiveFile = findHl7ArchiveByUuid(HL7Util.getHl7ArchivesDirectory(), uuid);
		//return the constructed hl7 in archive object
		if (hl7InArchiveFile != null)
			return constructHl7Archive(hl7InArchiveFile);
		return null;
		
	}
	
	/**
	 * @see HL7DAO#getAllHL7InArchivesInFileSystem()
	 */
	public List<HL7InArchive> getAllHL7InArchivesInFileSystem() throws DAOException {
		
		//get a list of all the files under the archives directory
		List<File> filesInArchivesDir = getAllFiles(HL7Util.getHl7ArchivesDirectory(), null);
		List<HL7InArchive> archivesList = null;
		
		if (filesInArchivesDir != null) {
			archivesList = new LinkedList<HL7InArchive>();
			Iterator<File> it = filesInArchivesDir.iterator();
			
			while (it.hasNext()) {
				File file = it.next();
				HL7InArchive archive = constructHl7Archive(file);
				if (archive != null)
					archivesList.add(archive);
				else
					log.warn("Failed to instatiate an hl7 archive for the file '" + file.getAbsoluteFile() + "'");
			}
		}
		
		return archivesList;
	}
	
	/**
	 * @see HL7DAO#deleteHL7InArchiveInFileSystem(Integer)
	 */
	public boolean deleteHL7InArchiveInFileSystem(String uuid) throws DAOException {
		
		File fileToDelete = findHl7ArchiveByUuid(HL7Util.getHl7ArchivesDirectory(), uuid);
		if (fileToDelete != null && fileToDelete.exists())
			return fileToDelete.delete();
		
		log.warn("No hl7 archive with id '" + uuid + "' was found");
		
		return false;
	}
	
	// THESE METHOD ARE NOT PART OF THE HL7 SERVICE INTERFACE
	//  They are called silently by the other methods in this class,
	//  to channel hl7 archive calls to the file system instead of the database.
	
	/**
	 * Writes a given hl7 archive to the file system
	 * 
	 * @param hl7InArchive the hl7 archive to write to the file system
	 * @return true if the archive was successfully written to the file system otherwise returns
	 *         false
	 */
	private boolean writeHL7InArchiveToFileSystem(HL7InArchive hl7InArchive) throws DAOException {
		
		PrintWriter writer = null;
		File destinatinDir = HL7Util.getHl7ArchivesDirectory();
		try {
			//write the archive to a separate file while grouping them according to
			//the year, month and date of month when they were stored in the archives table
			Calendar calendar = Calendar.getInstance(Context.getLocale());
			calendar.setTime(hl7InArchive.getDateCreated());
			//resolve the year folder from the date of creation of the archive
			File yearDir = new File(destinatinDir, Integer.toString(calendar.get(Calendar.YEAR)));
			if (!yearDir.isDirectory())
				yearDir.mkdirs();
			//resolve the appropriate month folder
			File monthDir = null;
			//for months jan to september, append a 0 at the beginning of the folder name i.e 01, 02,...., 09
			if (calendar.get(Calendar.MONTH) < 9)
				monthDir = new File(yearDir, "0" + Integer.toString(calendar.get(Calendar.MONTH) + 1));
			else
				monthDir = new File(yearDir, Integer.toString(calendar.get(Calendar.MONTH) + 1));
			
			if (!monthDir.isDirectory())
				monthDir.mkdirs();
			
			//resolve the appropriate day of month folder
			File dayDir = null;
			if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
				dayDir = new File(monthDir, "0" + Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
			else
				dayDir = new File(monthDir, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
			
			if (!dayDir.isDirectory())
				dayDir.mkdirs();
			
			if (StringUtils.isBlank(hl7InArchive.getUuid()) || hl7InArchive.getHL7Source() == null
			        || hl7InArchive.getHL7Source().getHL7SourceId() == null)
				throw new DAOException("Can't write archive with a null or empty uuid to the file system or no source key");
			
			//Using the archive's uuid, source id, value of date.getTime() in milliseconds
			//and source key(if present) to generate the file name
			File fileToWriteTo = new File(dayDir, hl7InArchive.getUuid() + "_"
			        + hl7InArchive.getHL7Source().getHL7SourceId() + "_" + calendar.getTimeInMillis()
			        + (StringUtils.isBlank(hl7InArchive.getHL7SourceKey()) ? "" : "_" + hl7InArchive.getHL7SourceKey())
			        + ".txt");
			
			writer = new PrintWriter(fileToWriteTo);
			//write the  hl7 data to the file
			writer.write(hl7InArchive.getHL7Data());
			
			//check if there was an error while writing to the current file
			if (writer.checkError()) {
				log.warn("An Error occured while writing hl7 archive with id '" + hl7InArchive.getHL7InArchiveId()
				        + "' to the file system");
				return false;
			}
			
		}
		catch (FileNotFoundException e) {
			log
			        .warn("Failed to write hl7 archive with id '" + hl7InArchive.getHL7InArchiveId()
			                + "' to the file system ", e);
			return false;
		}
		finally {
			if (writer != null)
				writer.close();
		}
		
		return true;
		
	}
	
	/**
	 * Convenience method that reads an hl7 in archive '.txt' file and constructs an Hl7InArchive
	 * object out of it.
	 * 
	 * @param hl7ArchiveFile the hl7 archive file to use to construct the object
	 * @return the constructed hl7 in archive object
	 */
	private HL7InArchive constructHl7Archive(File hl7ArchiveFile) throws DAOException {
		
		HL7InArchive hl7InArchive = null;
		
		if (hl7ArchiveFile != null && hl7ArchiveFile.isFile()) {
			if (!checkIfArchiveFilenameIsValid(hl7ArchiveFile))
				throw new DAOException("'" + hl7ArchiveFile.getAbsolutePath() + "' is an invalid hl7 archive file with.");
			
			Scanner filenameScanner = null;
			Scanner fileContentScanner = null;
			hl7InArchive = new HL7InArchive();
			try {
				filenameScanner = new Scanner(hl7ArchiveFile.getName());
				filenameScanner.useDelimiter("_");
				hl7InArchive.setUuid(filenameScanner.next());
				Integer sourceId = Integer.valueOf(filenameScanner.next());
				HL7Source source = Context.getHL7Service().getHL7Source(sourceId);
				
				if (source != null)
					hl7InArchive.setHL7Source(source);
				else
					log.warn("No hl7 source matches source id for hl7 archive file '" + hl7ArchiveFile.getAbsolutePath()
					        + "'");
				
				String milliSecondsString = filenameScanner.next();
				//if the name has no source key at the end of the file name, we need to remove the .txt at the end
				if (!filenameScanner.hasNext())
					milliSecondsString = milliSecondsString.substring(0, milliSecondsString.indexOf(".txt"));
				else {//then we have a source key at the end
					String sourceKeyWithFileExt = filenameScanner.next();
					hl7InArchive.setHL7SourceKey(sourceKeyWithFileExt.substring(0, sourceKeyWithFileExt.indexOf(".txt")));
				}
				
				hl7InArchive.setDateCreated(new Date(Long.valueOf(milliSecondsString)));
				
				fileContentScanner = new Scanner(hl7ArchiveFile);
				StringBuffer sb = new StringBuffer();
				while (fileContentScanner.hasNext()) {
					sb.append(fileContentScanner.nextLine() + System.getProperty("line.separator"));
				}
				
				hl7InArchive.setHL7Data(sb.toString());
				hl7InArchive.setMessageState(HL7Constants.HL7_STATUS_PROCESSED);
				
			}
			catch (NumberFormatException e) {
				log.warn("'" + hl7ArchiveFile.getName() + " is an invalid hl7 archive file name");
			}
			catch (FileNotFoundException e) {
				log.warn("The hl7 archive file '" + hl7ArchiveFile.getAbsolutePath() + " doesn't exist");
			}
			finally {
				if (filenameScanner != null) {
					if (filenameScanner.ioException() != null) {//if there was an error while reading filename
						log.warn("An error occurred while trying to read the file '" + hl7ArchiveFile.getAbsolutePath()
						        + "'");
					}
					filenameScanner.close();
				}
				if (fileContentScanner != null) {
					if (fileContentScanner.ioException() != null) {//if there was an error while reading file contents
						log.warn("An error occurred while trying to read the file '" + hl7ArchiveFile.getAbsolutePath()
						        + "'");
					}
					fileContentScanner.close();
				}
			}
			
		}
		
		return hl7InArchive;
	}
	
	/**
	 * Convenience method that recursively searches in the hl7 archives directory for the '.txt'
	 * file matching this uuid. E.g if uuid = 67, the match should have a file name of the form
	 * 67_sourceId_milliSec_sourcekey.txt
	 * 
	 * @param directoryToSearch the directory to search through
	 * @param the hl7 archives id
	 * @return the first matching file found
	 */
	private File findHl7ArchiveByUuid(File directoryToSearch, String uuid) throws DAOException {
		
		if (log.isDebugEnabled())
			log.debug("Searching in directory '" + directoryToSearch.getAbsolutePath() + "'");
		
		//search the files in this folder for the archive we want
		if (!StringUtils.isBlank(uuid) && directoryToSearch.isDirectory()) {
			final String searchUuid = uuid;
			File files[] = directoryToSearch.listFiles(new FileFilter() {
				
				//this filter is run against file names and not directory names
				@Override
				public boolean accept(File file) {
					//if it is a valid hl7 archive file and the uuid segment in the filename matches search uuid
					return (checkIfArchiveFilenameIsValid(file) && file.getName().substring(0, file.getName().indexOf("_"))
					        .equalsIgnoreCase(searchUuid));
				}
				
			});
			
			if (files != null) {
				//if there was exactly one file found, Wooooow!!!! this is the archive file we've been searching for
				if (files.length == 1)
					return files[0];
				else if (files.length > 1)//if multiple matches were found
					throw new DAOException("Multiple hl7 archive files were found for uuid '" + uuid + "'");
				
			}
			
			//at this point, the file wasn't in this directory,
			//get sub directories
			File subDirectories[] = directoryToSearch.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			
			if (subDirectories != null) {
				if (subDirectories.length > 0) {
					//look into all its sub folders recursively for the file
					for (File subDir : subDirectories) {
						//skip past empty folders
						if (subDir.listFiles().length > 0) {
							File foundFile = findHl7ArchiveByUuid(subDir, uuid);
							//if the file was found in this folder,
							if (foundFile != null)
								return foundFile;
						}
						
					}
				}
			}
			
		}
		
		//Well, looks like we don't have a match
		return null;
		
	}
	
	/**
	 * Checks if a given file is a valid hl7 archive file, i.e if it isn't a directory and the file
	 * name is of the form 'uuid_sourceId_milliSec[_optional source key].txt'
	 * 
	 * @param file the hl7 archive file to verify
	 * @return true only if the given file is a valid hl7 archive file otherwise returns false
	 */
	private static boolean checkIfArchiveFilenameIsValid(File file) {
		
		//Shouldn't be a folder, should be a '.txt' file, should have at least a '_' character which shouldn't be
		//the first and last character in the filename before the file extension '.txt', these last 3 conditions ensure that
		//the file name under test is written in the correct format we expect
		//i.e 'uuid_sourceId_milliSec[_optional source key].txt'
		return (file.isFile() && file.getName().endsWith(".txt") && file.getName().indexOf("_") > 0 && !file.getName()
		        .endsWith("_.txt"));
	}
	
	/**
	 * Looks into the given directory(the root directoryToSearch should be the default archives
	 * directory) and recursively searches for '.txt' files, since the method is recursive, we need
	 * to send it the list of files found in prior calls so that it adds to the same list
	 */
	private LinkedList<File> getAllFiles(File directoryToSearch, LinkedList<File> filesFound) throws DAOException {
		
		if (directoryToSearch != null) {
			if (!directoryToSearch.isDirectory())
				throw new DAOException("The file '" + directoryToSearch.getAbsolutePath() + "' is not a directory");
			
			if (filesFound == null)
				filesFound = new LinkedList<File>();
			
			//get a list of all files in this dir
			File filesInThisDirectory[] = directoryToSearch.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					//TODO narrow search to '.txt' files
					return file.isFile();
				}
			});
			
			if (filesInThisDirectory != null && filesInThisDirectory.length > 0) {
				//add the files in the this directory to the list files found so far
				for (File file : filesInThisDirectory) {
					filesFound.add(file);
				}
			}
			
			File subDirectories[] = directoryToSearch.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File file) {
					return file.isDirectory();
				}
			});
			
			if (subDirectories != null && subDirectories.length > 0) {
				//add all the files in each folder but don't return
				for (File file : subDirectories) {
					//note that we don't return
					getAllFiles(file, filesFound);
				}
			}
			
		}
		
		return filesFound;
		
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#isArchiveMigrationRequired()
	 */
	@Override
	public boolean isArchiveMigrationRequired() throws DAOException {
		//if migration has just been running, we need to empty the cache
		Context.flushSession();
		Connection conn = sessionFactory.getCurrentSession().connection();
		
		PreparedStatement ps = null;
		// check if the tables exist and contains rows.  If not, this has been run before
		try {
			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet tables = dbm.getTables(null, null, "hl7_in_archive", null);
			
			if (!tables.next()) {
				if (log.isDebugEnabled())
					log.debug("'hl7_in_archive' table doesn't exist");
				return false;
			}
			
			ps = conn.prepareStatement("select count(*) AS number_of_rows from hl7_in_archive");
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				int rows = rs.getInt("number_of_rows");
				if (rows == 0)
					return false;
			}
		}
		catch (SQLException e) {
			throw new DAOException("Error generated", e);
		}
		finally {
			if (ps != null) {
				try {
					ps.close();
				}
				catch (SQLException e) {
					log.warn("Error generated", e);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchiveByUuid(java.lang.String)
	 */
	@Override
	public HL7InArchive getHL7InArchiveByUuid(String uuid) throws DAOException {
		
		Query query = sessionFactory.getCurrentSession().createQuery("from HL7InArchive where uuid = ?").setParameter(0,
		    uuid, Hibernate.STRING);
		Object record = query.uniqueResult();
		if (record != null)
			return (HL7InArchive) record;
		
		return null;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#saveHL7InArchiveToFileSystem(org.openmrs.hl7.HL7InArchive)
	 */
	@Override
	public HL7InArchive saveHL7InArchiveToFileSystem(HL7InArchive hl7InArchive) throws DAOException {
		
		if (writeHL7InArchiveToFileSystem(hl7InArchive))
			return getHL7InArchiveInFileSystem(hl7InArchive.getUuid());
		
		return null;
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getHL7InArchiveByUuidFromFileSystem(java.lang.String)
	 */
	@Override
	public HL7InArchive getHL7InArchiveByUuidFromFileSystem(String uuid) throws DAOException {
		
		File archiveFile = findHl7ArchiveByUuid(HL7Util.getHl7ArchivesDirectory(), uuid);
		if (archiveFile != null)
			return constructHl7Archive(archiveFile);
		return null;
		
	}
	
	/**
	 * @see org.openmrs.hl7.db.HL7DAO#getAllHL7InArchives(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<HL7InArchive> getAllHL7InArchives(int maxResultsSetSize) {
		return sessionFactory.getCurrentSession().createQuery("from HL7InArchive order by HL7InArchiveId").setMaxResults(
		    maxResultsSetSize).list();
	}
	
}
