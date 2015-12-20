/*
 * Copyright (C) 2012 Openismus GmbH
 *
 * This file is part of GWT-Glom.
 *
 * GWT-Glom is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.glom.web.server;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.glom.web.server.libglom.Document;
import org.glom.web.shared.Documents;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * @author  Murray Cumming <murrayc@openismus.com>
 */
public class ConfiguredDocumentSet {
	private static final String GLOM_FILE_EXTENSION = "glom";

	private Hashtable<String, ConfiguredDocument> documentMapping = new Hashtable<String, ConfiguredDocument>();
	private Exception configurationException = null;

	/**
	 * 
	 */
	public ConfiguredDocumentSet() {
	}
	
	public ConfiguredDocument getDocument(final String documentID) {
		return documentMapping.get(documentID);
	}
	
	public Exception getConfigurationException() {
		return configurationException;
	}

	private static String getDocumentIdForFilename(final String filename) {
		// The key for the hash table is the file name without the .glom extension and with spaces ( ) replaced
		// with pluses (+). The space/plus replacement makes the key more friendly for URLs.
		return FilenameUtils.removeExtension(filename).replace(' ', '+');
	}

	private void addDocument(final ConfiguredDocument configuredDocument, final String documentID) {
		configuredDocument.setDocumentID(documentID);
		documentMapping.put(documentID, configuredDocument);
	}
	
	

	public void readConfiguration() throws ServletException {
	
		// All of the initialisation code is surrounded by a try/catch block so that the servlet can be in an
		// initialised state and the error message can be retrieved by the client code.
		try {
			// Find the configuration file. See this thread for background info:
			// http://stackoverflow.com/questions/2161054/where-to-place-properties-files-in-a-jsp-servlet-web-application
			final OnlineGlomProperties config = new OnlineGlomProperties();
			final InputStream is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("onlineglom.properties");
			if (is == null) {
				final String errorMessage = "onlineglom.properties not found.";
				Log.fatal(errorMessage);
				throw new Exception(errorMessage);
			}
			config.load(is); // can throw an IOException
	
			// check if we can read the configured glom file directory
			final String documentDirName = config.getDocumentsDirectory();
			final File documentDir = new File(documentDirName);
			if (!documentDir.isDirectory()) {
				final String errorMessage = documentDirName + " is not a directory.";
				Log.fatal(errorMessage);
				throw new Exception(errorMessage);
			}
			if (!documentDir.canRead()) {
				final String errorMessage = "Can't read the files in directory " + documentDirName + " .";
				Log.fatal(errorMessage);
				throw new Exception(errorMessage);
			}
	
			// get and check the glom files in the specified directory
			// TODO: Test this:
			final String[] extensions = { GLOM_FILE_EXTENSION };
			final Collection<?> glomFiles = FileUtils.listFiles(documentDir, extensions, true /* recursive */);
			if(!(glomFiles instanceof List<?>)) {
				final String errorMessage = "onlineglom.properties: listFiles() failed.";
				Log.fatal(errorMessage);
				throw new Exception(errorMessage);
			}
	
			// don't continue if there aren't any Glom files to configure
			if (glomFiles.size() <= 0) {
				final String errorMessage = "Unable to find any Glom documents in the configured directory "
						+ documentDirName
						+ " . Check the onlineglom.properties file to ensure that 'glom.document.directory' is set to the correct directory.";
				Log.error(errorMessage);
				throw new Exception(errorMessage);
			}
	
			// Check for a specified default locale,
			// for table titles, field titles, etc:
			final String globalLocaleID = StringUtils.defaultString(config.getGlobalLocale());
	
			for (final Object objGlomFile : glomFiles) {
				if(!(objGlomFile instanceof File)) {
					continue;
				}
				
				final File glomFile = (File)objGlomFile;
				final String filename = glomFile.getName();
	
				final String documentID = getDocumentIdForFilename(filename);
				final Document document = new Document(documentID);
				document.setFileURI("file://" + glomFile.getAbsolutePath());
				final boolean retval = document.load();
				if (!retval) {
					final String message = "An error occurred when trying to load file: " + glomFile.getAbsolutePath();
					Log.error(message);
					// continue with for loop because there may be other documents in the directory
					continue;
				}
	
				final ConfiguredDocument configuredDocument = new ConfiguredDocument(document); // can throw a
				// PropertyVetoException
	
				final String globalUserName = config.getGlobalUsername();
				final String globalPassword = config.getGlobalPassword();
	
				// check if a username and password have been set and work for the current document
				
				// Username/password could be set. Let's check to see if it works.
				ComboPooledDataSource authenticatedConnection = null;
				Credentials docCredentials = config.getCredentials(filename);

				if(docCredentials != null) {
					authenticatedConnection = SqlUtils.tryUsernameAndPassword(document, docCredentials.username, docCredentials.password); // can throw an SQLException
					if (authenticatedConnection != null) {
						//Use the document-specific credentials:
						docCredentials = new Credentials(document, docCredentials.username, docCredentials.password, authenticatedConnection);
					}
				}
	
				// Check the if the global username and password have been set and work with this document
				if (authenticatedConnection == null) {
					authenticatedConnection = SqlUtils.tryUsernameAndPassword(configuredDocument.getDocument(), globalUserName, globalPassword); // can throw an SQLException
					if(authenticatedConnection != null) {		
						//Use the global credentials:
						docCredentials = new Credentials(document, globalUserName, globalPassword, authenticatedConnection);
					}
				}
				
				// Store the credentials for the document,
				// also remembering the database connection for some time:
				if (authenticatedConnection != null) {
						configuredDocument.setCredentials(docCredentials);
				}
	
				if (!StringUtils.isEmpty(globalLocaleID)) {
					configuredDocument.setDefaultLocaleID(globalLocaleID.trim());
				}
	
				addDocument(configuredDocument, documentID);
			}
	
		} catch (final Exception e) {
			// Don't throw the Exception so that servlet will be initialised and the error message can be retrieved.
			configurationException = e;
		}
	
	}

	/**
	 * 
	 */
	public void forgetDocuments() {
		for (final String documentID : documentMapping.keySet()) {
			final ConfiguredDocument configuredDoc = getDocument(documentID);
			if (configuredDoc == null) {
				continue;
			}
		}
		
	}

	/**
	 * @return
	 */
	public Documents getDocuments() {
		final Documents documents = new Documents();
		for (final String documentID : documentMapping.keySet()) {
			final ConfiguredDocument configuredDoc = getDocument(documentID);
			if (configuredDoc == null) {
				continue;
			}

			final Document glomDocument = configuredDoc.getDocument();
			if (glomDocument == null) {
				final String errorMessage = "getDocuments(): getDocument() failed.";
				Log.fatal(errorMessage);
				// TODO: throw new Exception(errorMessage);
				continue;
			}

			final String localeID = StringUtils.defaultString(configuredDoc.getDefaultLocaleID());
			documents.addDocument(documentID, glomDocument.getDatabaseTitle(localeID), localeID);
		}
		return documents;
	}
}