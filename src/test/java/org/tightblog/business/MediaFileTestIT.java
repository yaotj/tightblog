/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.business;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.tightblog.WebloggerTest;
import org.tightblog.pojos.MediaDirectory;
import org.tightblog.pojos.MediaFile;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Test media file related business operations.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MediaFileTestIT extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;

    private static final String TEST_IMAGE = "/hawk.jpg";

    @Value("${mediafiles.storage.dir}")
    private String mediafileDir;

    @Resource
    private MediaFileManager mediaFileManager;

    public void setMediaFileManager(MediaFileManager mediaFileManager) {
        this.mediaFileManager = mediaFileManager;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("mediaFileTestUser");
        testWeblog = setupWeblog("media-file-test-weblog", testUser);
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
    }

    /**
     * Test creation of directory by path
     */
    @Test
    public void testCreateMediaDirectoryByPath() throws Exception {

        try {
            mediaFileManager.createMediaDirectory(testWeblog, "");
            fail("did not fail with invalid name");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            mediaFileManager.createMediaDirectory(testWeblog, "default");
            fail("did not fail with duplicate name");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        MediaDirectory newDirectory1 = mediaFileManager.createMediaDirectory(testWeblog, "test1");
        MediaDirectory newDirectory2 = mediaFileManager.createMediaDirectory(testWeblog, "test2");

        MediaDirectory newDirectory1ById = mediaDirectoryRepository.findByIdOrNull(newDirectory1.getId());
        assertEquals(newDirectory1, newDirectory1ById);

        MediaDirectory newDirectory2ById = mediaDirectoryRepository.findByIdOrNull(newDirectory2.getId());
        assertEquals("test2", newDirectory2ById.getName());

        // show throw error when creating directory that already exists
        try {
            mediaFileManager.createMediaDirectory(testWeblog, "test1");
            fail("should not have allowed creation of directory with same name");
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Test getting list of all directories for a given user.
     */
    @Test
    public void testGetMediaDirectories() throws Exception {
        mediaFileManager.createMediaDirectory(testWeblog, "dir1");
        mediaFileManager.createMediaDirectory(testWeblog, "dir2");

        List<MediaDirectory> directories = mediaDirectoryRepository.findByWeblog(testWeblog);
        assertNotNull(directories);
        assertEquals(3, directories.size());
        assertTrue(containsName(directories, "default"));
        assertTrue(containsName(directories, "dir1"));
        assertTrue(containsName(directories, "dir2"));
    }

    /**
     * Test utility to determine whether the given list of directories contains
     * a directory of given path.
     */
    private boolean containsName(Collection<MediaDirectory> directories,
            String name) {
        for (MediaDirectory directory : directories) {
            if (name.equals(directory.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test utility to determine whether a list of files contains a file with
     * given name.
     */
    private boolean containsFileWithName(Collection<MediaFile> files, String name) {
        for (MediaFile file : files) {
            if (name.equals(file.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test deletion of media file
     */
    @Test
    public void testDeleteMediaFile() throws Exception {
        MediaDirectory rootDirectory = mediaDirectoryRepository.findByWeblogAndName(testWeblog, "default");

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setName("test4.jpg");
        mediaFile.setNotes("This is a test image 4");
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setContentType("image/jpeg");

        mediaFileManager.saveMediaFile(mediaFile, getClass().getResourceAsStream(TEST_IMAGE), null);
        String id = mediaFile.getId();
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        MediaFile mediaFile1 = mediaFileRepository.findByIdOrNull(id);

        assertEquals("test4.jpg", mediaFile1.getName());
        mediaFileManager.removeMediaFile(testWeblog, mediaFile1);

        MediaFile mediaFile2 = mediaFileRepository.findByIdOrNull(id);
        assertNull(mediaFile2);

        File flag = new File(mediafileDir + File.separator + "migration-status.properties");
        flag.delete();
    }

    /**
     * Test creation of media file.
     */
    @Test
    public void teststoreMediaFile() throws Exception {
        MediaDirectory rootDirectory = mediaDirectoryRepository.findByWeblogAndName(testWeblog, "default");
        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setName("test.jpg");
        mediaFile.setNotes("This is a test image");
        mediaFile.setLength(2000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setContentType("image/jpeg");
        rootDirectory.getMediaFiles().add(mediaFile);

        mediaFileManager.saveMediaFile(mediaFile, getClass().getResourceAsStream(TEST_IMAGE), null);
        assertNotNull(mediaFile.getId());
        assertTrue(mediaFile.getId().length() > 0);

        MediaFile mediaFile1 = mediaFileRepository.findByIdOrNull(mediaFile.getId());
        assertEquals("test.jpg", mediaFile1.getName());
        assertEquals("This is a test image", mediaFile1.getNotes());
        assertEquals(2000, mediaFile1.getLength());
    }

    /**
     * Test media file update
     */
    @Test
    public void testUpdateMediaFile() throws Exception {
        MediaDirectory rootDirectory = mediaDirectoryRepository.findByWeblogAndName(testWeblog, "default");
        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setName("test5.jpg");
        mediaFile.setNotes("This is a test image 5");
        mediaFile.setLength(3000);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setContentType("image/jpeg");
        mediaFileManager.saveMediaFile(mediaFile, getClass().getResourceAsStream(TEST_IMAGE), null);

        rootDirectory.getMediaFiles().add(mediaFile);
        String id = mediaFile.getId();
        assertNotNull(id);
        assertNotNull(id.length() > 0);

        MediaFile mediaFile1 = mediaFileRepository.findByIdOrNull(id);
        mediaFile1.setName("updated.gif");
        mediaFile1.setNotes("updated desc");
        mediaFile1.setContentType("image/gif");
        mediaFileManager.saveMediaFile(mediaFile1, null, null);

        MediaFile mediaFile2 = mediaFileRepository.findByIdOrNull(id);
        assertEquals("updated.gif", mediaFile2.getName());
        assertEquals("updated desc", mediaFile2.getNotes());
        assertEquals("image/gif", mediaFile2.getContentType());
    }

    /**
     * Test media file and directory gets
     */
    @Test
    public void testGetDirectoryContents() throws Exception {
        mediaFileManager.createMediaDirectory(testWeblog, "dir1");
        mediaFileManager.createMediaDirectory(testWeblog, "dir2");
        mediaFileManager.createMediaDirectory(testWeblog, "dir3");

        MediaDirectory rootDirectory = mediaDirectoryRepository.findByWeblogAndName(testWeblog, "default");

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setName("test6_1.jpg");
        mediaFile.setNotes("This is a test image 6.1");
        mediaFile.setLength(4000);
        mediaFile.setContentType("image/jpeg");
        mediaFileManager.saveMediaFile(mediaFile, getClass().getResourceAsStream(TEST_IMAGE), null);

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setCreator(testUser);
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setName("test6_2.jpg");
        mediaFile2.setNotes("This is a test image 6.2");
        mediaFile2.setLength(4000);
        mediaFile2.setContentType("image/jpeg");
        mediaFileManager.saveMediaFile(mediaFile2, getClass().getResourceAsStream(TEST_IMAGE), null);

        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());

        List<MediaDirectory> childDirectories = testWeblog.getMediaDirectories();
        assertEquals(4, childDirectories.size());
        assertTrue(containsName(childDirectories, "dir1"));
        assertTrue(containsName(childDirectories, "dir2"));
        assertTrue(containsName(childDirectories, "dir3"));

        Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test6_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test6_2.jpg"));

        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());
        assertTrue(rootDirectory.hasMediaFile("test6_1.jpg"));
        assertTrue(rootDirectory.hasMediaFile("test6_2.jpg"));
    }

    /**
     * Test moving files across directories.
     */
    @Test
    public void testMoveDirectoryContents() throws Exception {
        MediaDirectory dir1 = mediaFileManager.createMediaDirectory(testWeblog, "dir1");
        mediaFileManager.createMediaDirectory(testWeblog, "dir2");
        mediaFileManager.createMediaDirectory(testWeblog, "dir3");

        MediaDirectory rootDirectory = mediaDirectoryRepository.findByWeblogAndName(testWeblog, "default");

        MediaFile mediaFile = new MediaFile();
        mediaFile.setCreator(testUser);
        mediaFile.setDirectory(rootDirectory);
        mediaFile.setName("test7_1.jpg");
        mediaFile.setNotes("This is a test image 7.1");
        mediaFile.setLength(4000);
        mediaFile.setContentType("image/jpeg");
        mediaFileManager.saveMediaFile(mediaFile, getClass().getResourceAsStream(TEST_IMAGE), null);

        MediaFile mediaFile2 = new MediaFile();
        mediaFile2.setCreator(testUser);
        mediaFile2.setDirectory(rootDirectory);
        mediaFile2.setName("test7_2.jpg");
        mediaFile2.setNotes("This is a test image 7.2");
        mediaFile2.setLength(4000);
        mediaFile2.setContentType("image/jpeg");
        mediaFileManager.saveMediaFile(mediaFile2, getClass().getResourceAsStream(TEST_IMAGE), null);

        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());

        Set<MediaFile> mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

        MediaDirectory targetDirectory = mediaDirectoryRepository.findByIdOrNull(dir1.getId());
        mediaFileManager.moveMediaFiles(mediaFiles, targetDirectory);

        rootDirectory = mediaDirectoryRepository.findByIdOrNull(rootDirectory.getId());
        targetDirectory = mediaDirectoryRepository.findByIdOrNull(dir1.getId());

        mediaFiles = targetDirectory.getMediaFiles();
        assertEquals(2, mediaFiles.size());
        assertTrue(containsFileWithName(mediaFiles, "test7_1.jpg"));
        assertTrue(containsFileWithName(mediaFiles, "test7_2.jpg"));

        mediaFiles = rootDirectory.getMediaFiles();
        assertEquals(0, mediaFiles.size());
    }

    /**
     * Test deletion of media file folder association with named queries
     */
    @Test
    public void testDirectoryDeleteAssociation() {
        MediaDirectory dir1 = mediaFileManager.createMediaDirectory(testWeblog, "dir1");
        mediaFileManager.createMediaDirectory(testWeblog, "dir2");
        mediaFileManager.createMediaDirectory(testWeblog, "dir3");

        List<MediaDirectory> directories = testWeblog.getMediaDirectories();
        assertEquals(4, directories.size());

        // Delete folder
        MediaDirectory directoryById = mediaDirectoryRepository.findByIdOrNull(dir1.getId());
        testWeblog.getMediaDirectories().remove(directoryById);
        mediaFileManager.removeAllFiles(directoryById);
        weblogRepository.saveAndFlush(testWeblog);

        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertEquals(3, testWeblog.getMediaDirectories().size());
    }
}