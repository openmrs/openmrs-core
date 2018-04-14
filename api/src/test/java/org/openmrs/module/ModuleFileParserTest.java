/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Tests {@link ModuleFileParser} with a database and file IO.
 * Mostly deprecated methods are tested but also contains one integration style test parsing the logic module from test resources.
 */
public class ModuleFileParserTest extends BaseContextSensitiveTest {

	private static final String LOGIC_MODULE_PATH = "org/openmrs/module/include/logic-0.2.omod";

	private static DocumentBuilderFactory documentBuilderFactory;

	private static DocumentBuilder documentBuilder;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Autowired
	MessageSourceService messageSourceService;

	@BeforeClass
	public static void setUp() throws ParserConfigurationException {
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
	}

	@Test
	public void moduleFileParser_shouldFailCreatingParserFromFileIfGivenNull() {

		expectModuleExceptionWithTranslatedMessage("Module.error.fileCannotBeNull");

		new ModuleFileParser((File) null);
	}

	@Test
	public void moduleFileParser_shouldFailCreatingParserFromFileIfNotEndingInOmod() {

		expectModuleExceptionWithTranslatedMessage("Module.error.invalidFileExtension");

		new ModuleFileParser(new File("reporting.jar"));
	}

	@Test
	public void moduleFileParser_shouldFailCreatingParserFromFileIfInputStreamClosed() throws IOException {

		File moduleFile = new File(getClass().getClassLoader().getResource(LOGIC_MODULE_PATH).getPath());

		InputStream inputStream = new FileInputStream(moduleFile);
		inputStream.close();

		expectModuleExceptionWithTranslatedMessage("Module.error.cannotCreateFile");

		new ModuleFileParser(inputStream);
	}

	@Test
	public void parse_shouldParseValidXmlConfigCreatedFromInputStream() throws IOException {

		File moduleFile = new File(getClass().getClassLoader().getResource(LOGIC_MODULE_PATH).getPath());

		ModuleFileParser parser = new ModuleFileParser(new FileInputStream(moduleFile));

		Module module = parser.parse();

		assertThat(module.getModuleId(), is("logic"));
		assertThat(module.getVersion(), is("0.2"));
		assertThat(module.getPackageName(), is("org.openmrs.logic"));
		assertThat(module.getActivatorName(), is("org.openmrs.logic.LogicModuleActivator"));
		assertThat(module.getMappingFiles().size(), is(1));
		assertThat(module.getMappingFiles(), hasItems("LogicRuleToken.hbm.xml"));
	}

	@Test
	public void parse_shouldFailIfModuleHasConfigInvalidConfigVersion() throws Exception {
		// This test needs to be in a BaseContextSensitive test class
		// since the implementation uses Context.getLocale()
		// since thats static we would need to use PowerMock but then this
		// would not show in our coverage.
		// TODO - remove use of Context.getLocale() - by for example
		// implementing MessageSourceService.getMessage(String key, Object[])
		// which takes care of getting the users current locale and allows us
		// to mock it using mockito

		String invalidConfigVersion = "0.0.1";
		String expectedMessage = messageSourceService
			.getMessage("Module.error.invalidConfigVersion",
				new Object[] { invalidConfigVersion, "1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6" }, Context.getLocale());
		expectModuleExceptionWithMessage(expectedMessage);

		Document configXml = documentBuilder.newDocument();
		Element root = configXml.createElement("module");
		configXml.appendChild(root);
		configXml.getDocumentElement().setAttribute("configVersion", invalidConfigVersion);

		ModuleFileParser parser = new ModuleFileParser(writeConfigXmlToFile(configXml));

		parser.parse();
	}

	@Test
	public void parse_shouldParseValidLogicModuleFromFile() {

		File moduleFile = new File(getClass().getClassLoader().getResource(LOGIC_MODULE_PATH).getPath());
		ModuleFileParser parser = new ModuleFileParser(Context.getMessageSourceService());

		Module module = parser.parse(moduleFile);

		assertThat(module.getModuleId(), is("logic"));
		assertThat(module.getVersion(), is("0.2"));
		assertThat(module.getPackageName(), is("org.openmrs.logic"));
		assertThat(module.getActivatorName(), is("org.openmrs.logic.LogicModuleActivator"));
		assertThat(module.getMappingFiles().size(), is(1));
		assertThat(module.getMappingFiles(), hasItems("LogicRuleToken.hbm.xml"));
	}

	private void expectModuleExceptionWithTranslatedMessage(String s) {
		String expectedMessage = messageSourceService.getMessage(s);
		expectModuleExceptionWithMessage(expectedMessage);
	}

	private void expectModuleExceptionWithMessage(String s) {
		String expectedMessage = messageSourceService.getMessage(s);
		expectedException.expect(ModuleException.class);
		expectedException.expectMessage(expectedMessage);
	}

	private File writeConfigXmlToFile(Document config) throws IOException {
		File file = temporaryFolder.newFile("modulefileparsertest.omod");
		JarOutputStream jar = createJarWithConfigXmlEntry(file);
		writeConfigXmlToJar(jar, config);
		return file;
	}

	private JarOutputStream createJarWithConfigXmlEntry(File file) throws IOException {
		JarOutputStream jar = new JarOutputStream(new FileOutputStream(file));
		ZipEntry config = new ZipEntry("config.xml");
		jar.putNextEntry(config);
		return jar;
	}

	private void writeConfigXmlToJar(JarOutputStream jar, Document config) throws IOException {
		jar.write(getByteArray(config));
		jar.closeEntry();
		jar.close();
	}

	private byte[] getByteArray(Document config) {
		DOMImplementationLS impl = (DOMImplementationLS) config.getImplementation();
		LSSerializer serializer = impl.createLSSerializer();
		LSOutput out = impl.createLSOutput();
		out.setEncoding("UTF-8");
		Writer stringWriter = new StringWriter();
		out.setCharacterStream(stringWriter);
		serializer.write(config, out);
		return stringWriter.toString().getBytes();
	}
}
