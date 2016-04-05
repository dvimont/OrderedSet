/*
 * Copyright (C) 2016 Daniel Vimont
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.commonvox.collections;

import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test all protected and package-private methods of MapNode.
 *
 * @author Daniel Vimont
 */
public class TestMapNode {

    private final static String TITLE_MAPNODE_TITLE = "Books by Title";
    private final static String AUTHOR_MAPNODE_TITLE = "Books by Author/Title";
    private final static String GENRE_MAPNODE_TITLE
            = "Books by Genre/Author/Title";
    private static Method AUTHOR_GET_METHOD;
    private static Method TITLE_GET_METHOD;
    private static Method SUBTITLE_GET_METHOD;
    private static Method GENRE_GET_METHOD;
    static {
      try {
        AUTHOR_GET_METHOD
                = Book.class.getDeclaredMethod("getAuthors");
        TITLE_GET_METHOD
                = Book.class.getDeclaredMethod("getTitle");
        SUBTITLE_GET_METHOD
                = Book.class.getDeclaredMethod("getSubTitle");
        GENRE_GET_METHOD
                = Book.class.getDeclaredMethod("getGenres");
      }
      catch (NoSuchMethodException ex) {
      }
    }
    private final static String TITLE_FAILURE_MSG
            = "Title set/get failure in " + MapNode.class.getSimpleName();
    private final static String DEPTH_FAILURE_MSG
            = "Depth set/get failure in " + MapNode.class.getSimpleName();
    private final static String TOP_LEVEL_NODE_FAILURE_MSG
            = "IS_TOP_LEVEL_NODE set failure in " + MapNode.class.getSimpleName();
    private final static String KEY_COMPONENT_FAILURE_MSG
            = "KeyComponent set/get method failure in "
                + MapNode.class.getSimpleName();
    private final static String GET_TITLE_METHOD_NAME = "getTitle";
    private final static String GET_SUBTITLE_METHOD_NAME = "getSubTitle";
    private final static String GET_AUTHORS_METHOD_NAME = "getAuthors";
    private final static String GET_GENRES_METHOD_NAME = "getGenres";
    private MapNode titleMapNode;
    private MapNode authorMapNode;
    private MapNode genreMapNode;
    private MapNode titleMethodBasedMapNode;
    private MapNode authorMethodBasedMapNode;
    private MapNode genreMethodBasedMapNode;

    @Test
    public void testConstructors()
        throws NoSuchMethodException {
      constructUsingClasses();
      constructUsingMethods();
    }

    private void constructUsingClasses() {
        titleMapNode = new MapNode<Book>(TITLE_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Book.Title.class));
        assertEquals(TITLE_FAILURE_MSG, TITLE_MAPNODE_TITLE,
                titleMapNode.getTitle());
        List<KeyComponentProfile> keyComponentProfiles
                = titleMapNode.getKeyComponentProfileList();
        // class check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 2, keyComponentProfiles.size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Book.Title.class, keyComponentProfiles.get(0).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Object.class, keyComponentProfiles.get(1).getKeyComponentClass());
        // method check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 2,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().size());
        assertTrue(KEY_COMPONENT_FAILURE_MSG,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(0).
                        equals(TITLE_GET_METHOD) ||
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(0).
                        equals(SUBTITLE_GET_METHOD));
        assertTrue(KEY_COMPONENT_FAILURE_MSG,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(1).
                        equals(TITLE_GET_METHOD) ||
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(1).
                        equals(SUBTITLE_GET_METHOD));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                KeyComponentProfile.KeyComponentBasis.IDENTITY,
                keyComponentProfiles.get(1).getKeyComponentBasis());

        authorMapNode = new MapNode<Book>(AUTHOR_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Author.class),
                new KeyComponentProfile<Book>(Book.class, Book.Title.class));
        assertEquals(TITLE_FAILURE_MSG, AUTHOR_MAPNODE_TITLE,
                authorMapNode.getTitle());
        keyComponentProfiles = authorMapNode.getKeyComponentProfileList();
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 3, keyComponentProfiles.size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Author.class,
                keyComponentProfiles.get(0).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Book.Title.class,
                keyComponentProfiles.get(1).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Object.class,
                keyComponentProfiles.get(2).getKeyComponentClass());
        // method check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, AUTHOR_GET_METHOD,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(0));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 2,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().size());
        assertTrue(KEY_COMPONENT_FAILURE_MSG,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().get(0).
                        equals(TITLE_GET_METHOD) ||
                keyComponentProfiles.get(1).getKeyComponentGetMethods().get(0).
                        equals(SUBTITLE_GET_METHOD));
        assertTrue(KEY_COMPONENT_FAILURE_MSG,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().get(1).
                        equals(TITLE_GET_METHOD) ||
                keyComponentProfiles.get(1).getKeyComponentGetMethods().get(1).
                        equals(SUBTITLE_GET_METHOD));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(2).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                KeyComponentProfile.KeyComponentBasis.IDENTITY,
                keyComponentProfiles.get(2).getKeyComponentBasis());

        genreMapNode = new MapNode<Book>(GENRE_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Genre.class),
                new KeyComponentProfile<Book>(Book.class, Author.class),
                new KeyComponentProfile<Book>(Book.class, Book.Title.class));
        assertEquals(TITLE_FAILURE_MSG, GENRE_MAPNODE_TITLE,
                genreMapNode.getTitle());
        keyComponentProfiles = genreMapNode.getKeyComponentProfileList();
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 4, keyComponentProfiles.size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Genre.class, keyComponentProfiles.get(0).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Author.class, keyComponentProfiles.get(1).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Book.Title.class, keyComponentProfiles.get(2).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Object.class, keyComponentProfiles.get(3).getKeyComponentClass());

        assertEquals(DEPTH_FAILURE_MSG, 2, titleMapNode.getDepth());
        assertEquals(DEPTH_FAILURE_MSG, 3, authorMapNode.getDepth());
        assertEquals(DEPTH_FAILURE_MSG, 4, genreMapNode.getDepth());

        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                titleMapNode.isTopLevelNode());
        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                authorMapNode.isTopLevelNode());
        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                genreMapNode.isTopLevelNode());
    }

    private void constructUsingMethods()
          throws NoSuchMethodException {

        titleMethodBasedMapNode = new MapNode<Book>(TITLE_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Book.Title.class,
                        TITLE_GET_METHOD));
        assertEquals(TITLE_FAILURE_MSG, TITLE_MAPNODE_TITLE,
                titleMethodBasedMapNode.getTitle());
        List<KeyComponentProfile> keyComponentProfiles
                = titleMethodBasedMapNode.getKeyComponentProfileList();
        // class check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 2, keyComponentProfiles.size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Book.Title.class, keyComponentProfiles.get(0).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                Object.class, keyComponentProfiles.get(1).getKeyComponentClass());
        // method check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, TITLE_GET_METHOD,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(0));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                KeyComponentProfile.KeyComponentBasis.IDENTITY,
                keyComponentProfiles.get(1).getKeyComponentBasis());

        authorMethodBasedMapNode = new MapNode<Book>(AUTHOR_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Author.class,
                        AUTHOR_GET_METHOD),
                new KeyComponentProfile<Book>(Book.class, Book.Title.class,
                        TITLE_GET_METHOD));
        assertEquals(TITLE_FAILURE_MSG, AUTHOR_MAPNODE_TITLE,
                authorMethodBasedMapNode.getTitle());
        keyComponentProfiles = authorMethodBasedMapNode.getKeyComponentProfileList();
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 3, keyComponentProfiles.size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Author.class,
                keyComponentProfiles.get(0).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Book.Title.class,
                keyComponentProfiles.get(1).getKeyComponentClass());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, Object.class,
                keyComponentProfiles.get(2).getKeyComponentClass());
        // method check
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, AUTHOR_GET_METHOD,
                keyComponentProfiles.get(0).getKeyComponentGetMethods().get(0));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG, TITLE_GET_METHOD,
                keyComponentProfiles.get(1).getKeyComponentGetMethods().get(0));
        assertEquals(KEY_COMPONENT_FAILURE_MSG, 1,
                keyComponentProfiles.get(2).getKeyComponentGetMethods().size());
        assertEquals(KEY_COMPONENT_FAILURE_MSG,
                KeyComponentProfile.KeyComponentBasis.IDENTITY,
                keyComponentProfiles.get(2).getKeyComponentBasis());

        genreMethodBasedMapNode = new MapNode<Book>(GENRE_MAPNODE_TITLE,
                new KeyComponentProfile<Book>(Book.class, Genre.class,
                        GENRE_GET_METHOD),
                new KeyComponentProfile<Book>(Book.class, Author.class,
                        AUTHOR_GET_METHOD),
                new KeyComponentProfile<Book>(Book.class, Book.Title.class,
                        TITLE_GET_METHOD));

        assertEquals(DEPTH_FAILURE_MSG, 2, titleMethodBasedMapNode.getDepth());
        assertEquals(DEPTH_FAILURE_MSG, 3, authorMethodBasedMapNode.getDepth());
        assertEquals(DEPTH_FAILURE_MSG, 4, genreMethodBasedMapNode.getDepth());

        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                titleMethodBasedMapNode.isTopLevelNode());
        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                authorMethodBasedMapNode.isTopLevelNode());
        assertEquals(TOP_LEVEL_NODE_FAILURE_MSG, true,
                genreMethodBasedMapNode.isTopLevelNode());
   }
}
