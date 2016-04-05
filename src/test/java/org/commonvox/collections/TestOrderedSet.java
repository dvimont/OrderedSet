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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Daniel Vimont
 */
public class TestOrderedSet {

  private OrderedSet<Book> bookOrderSet;
  private OrderedSet<Book> titleOrderSet;
  private OrderedSet<Book> authorOrderSet;
  private OrderedSet<Book> authorFirstNameOrderSet;
  private OrderedSet<Book> genreOrderSet;
  private OrderedSet<Book> nonComparativeOrderSet;
  private String componentBasis;

  private final static String TITLE_OR_SUBTITLE_INDEX_TITLE
          = "Books indexed by Title or Subtitle";
  private final static String AUTHOR_INDEX_TITLE = "Books by Author/Title";
  private final static String GENRE_INDEX_TITLE
          = "Books by Genre/Author/Title";
  private final static String UNINDEXABLE_INDEX_TITLE = "Unindexable index";
  String failureMsg = "";
  private static final int CELL_WIDTH = 25;
  private static final String CELL_BORDER =
          String.format("%1$" + CELL_WIDTH + "s", "").replace(' ', '-');
  private static final char BAR = '|';

  private static Method AUTHOR_GET_METHOD;
  private static Method TITLE_GET_METHOD;
  private static Method SUBTITLE_GET_METHOD;
  private static Method GENRE_GET_METHOD;
  private static Method NONCOMPARABLE_GET_METHOD;
  private static KeyComponentProfile<Book> TITLE_KEY_COMPONENT;
  private static KeyComponentProfile<Book> AUTHOR_KEY_COMPONENT;
  private static KeyComponentProfile<Book> AUTHOR_FIRST_NAME_KEY_COMPONENT;
  private static KeyComponentProfile<Book> GENRE_KEY_COMPONENT;
  private static KeyComponentProfile<Book> BOOK_KEY_COMPONENT;

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
      NONCOMPARABLE_GET_METHOD
              = Book.class.getDeclaredMethod("getNoncomparableTitle");
    }
    catch (NoSuchMethodException ex) {
      throw new RuntimeException("Invalid Method object configuration for test!");
    }
  }

  @Test
  public void testInitiallyEmptySetConstructor() {
    testInitiallyEmptySetConstructor(false);
  }

  @Test
  public void testInitiallyEmptySetConstructorWithSubclass() {
    testInitiallyEmptySetConstructor(true);
  }

  private void testInitiallyEmptySetConstructor(boolean useSubclass) {

    for (int runType = 1; runType <= 3; runType++) {
      constructKeyComponentProfiles(runType);
      bookOrderSet = new OrderedSet<Book>(BOOK_KEY_COMPONENT);
      titleOrderSet = new OrderedSet<Book>(TITLE_KEY_COMPONENT);
      authorOrderSet = new OrderedSet<Book>(
              AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      authorFirstNameOrderSet = new OrderedSet<Book>(
              AUTHOR_FIRST_NAME_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      genreOrderSet = new OrderedSet<Book>(
              GENRE_KEY_COMPONENT, AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);

      List<Book> bookList = getRandomOrderBookList(useSubclass);
      int bookCount = 0;
      TreeMap<Book.NoncomparableTitle, Book> nonCompTitleHashCodeMap
              = new TreeMap<Book.NoncomparableTitle, Book>(new HashCodeComparator());

      // Add first book in bookList to OrderedSets and perform verifications.
      bookOrderSet.add(bookList.get(0));
      titleOrderSet.add(bookList.get(0));
      authorOrderSet.add(bookList.get(0));
      authorFirstNameOrderSet.add(bookList.get(0));
      genreOrderSet.add(bookList.get(0));
      nonCompTitleHashCodeMap.put(
              bookList.get(0).getNoncomparableTitle(), bookList.get(0));
      bookCount++;
      failureMsg = "FAILURE in add/get of a single object to an "
              + "initially empty OrderedSet based on " + componentBasis;

      List<Book> bookQueryResult = bookOrderSet.values();
      assertEquals(failureMsg, bookCount, bookQueryResult.size());
      assertEquals(failureMsg, bookList.get(0), bookQueryResult.get(0));

      List<Book> bookByTitleQueryResult = titleOrderSet.values();
      if (runType == 3) {
        assertEquals(failureMsg, bookCount * 1, bookByTitleQueryResult.size());
      } else {
        assertEquals(failureMsg, bookCount * 2, bookByTitleQueryResult.size());
      }

      List<Book> bookByAuthorQueryResult = authorOrderSet.values();
      if (runType == 3) {
        assertEquals(failureMsg, bookCount * 1, bookByAuthorQueryResult.size());
      } else {
        assertEquals(failureMsg, bookCount * 2, bookByAuthorQueryResult.size());
      }

      List<Book> bookByAuthorFirstNameQueryResult = authorFirstNameOrderSet.values();
      if (runType == 3) {
        assertEquals(failureMsg, bookCount * 1,
                bookByAuthorFirstNameQueryResult.size());
      } else {
        assertEquals(failureMsg, bookCount * 2,
                bookByAuthorFirstNameQueryResult.size());
      }

      List<Book> bookByGenreQueryResult = genreOrderSet.values();
      if (runType == 3) {
        assertEquals(failureMsg, bookCount * 2, bookByGenreQueryResult.size());
      } else {
        assertEquals(failureMsg, bookCount * 4, bookByGenreQueryResult.size());
      }

      Set<Object> titleQueryResult
              = titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT);
      for (Object title : titleQueryResult) {
        if (runType == 3) {
          assertEquals(failureMsg,
                  bookList.get(0).getNoncomparableTitle(),
                  (Book.NoncomparableTitle) title);
        } else {
          assertTrue(failureMsg,
                  (bookList.get(0).getTitle().equals((Book.Title) title)
                  || bookList.get(0).getSubTitle().equals(
                          (Book.Title) title)));
        }
      }
      if (runType != 3) {
        assertTrue(failureMsg,
                titleQueryResult.contains(bookList.get(0).getTitle()));
        assertTrue(failureMsg,
                titleQueryResult.contains(bookList.get(0).getSubTitle()));
      }

      Set<Object> authorQueryResult
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT);
      for (Object author : authorQueryResult) {
        assertTrue(failureMsg,
                bookList.get(0).getAuthors().contains((Author) author));
      }
      for (Author authorFromList : bookList.get(0).getAuthors()) {
        assertTrue(failureMsg, authorQueryResult.contains(authorFromList));
      }

      Set<Object> authorFirstNameQueryResult
              = authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT);
      for (Object author : authorFirstNameQueryResult) {
        assertTrue(failureMsg,
                bookList.get(0).getAuthors().contains((Author) author));
      }
      for (Author authorFromList : bookList.get(0).getAuthors()) {
        assertTrue(failureMsg,
                authorFirstNameQueryResult.contains(authorFromList));
      }

      Set<Object> genreQueryResult
              = genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT);
      for (Object genre : genreQueryResult) {
        assertTrue(failureMsg,
                bookList.get(0).getGenres().contains((Genre) genre));
      }
      for (Genre genreFromList : bookList.get(0).getGenres()) {
        assertTrue(failureMsg, genreQueryResult.contains(genreFromList));
      }

      // Add third book in bookList to OrderedSets and perform verifications.
      failureMsg = "FAILURE in add/get of a SECOND object to an "
              + "initially empty OrderedSet based on " + componentBasis;
      bookOrderSet.add(bookList.get(2));
      titleOrderSet.add(bookList.get(2));
      authorOrderSet.add(bookList.get(2));
      authorFirstNameOrderSet.add(bookList.get(2));
      genreOrderSet.add(bookList.get(2));
      nonCompTitleHashCodeMap.put(
              bookList.get(2).getNoncomparableTitle(), bookList.get(2));
      bookCount++;
      bookQueryResult = bookOrderSet.values();
      assertEquals(failureMsg, bookCount, bookQueryResult.size());
      int bookIndex = 0;
      for (Book retrievedBook : bookQueryResult) {
        switch (bookIndex) {
          case 0:
            assertEquals(failureMsg, bookList.get(0), retrievedBook);
            break;
          case 1:
            assertEquals(failureMsg, bookList.get(2), retrievedBook);
            break;
        }
        bookIndex++;
      }

      bookByTitleQueryResult = titleOrderSet.values();
      if (runType == 3) {
        assertEquals(
                failureMsg, bookCount, bookByTitleQueryResult.size());
        if (bookCount == bookByTitleQueryResult.size()) {
          Iterator<Book> hashMapIterator
                  = nonCompTitleHashCodeMap.values().iterator();
          for (Book book : bookByTitleQueryResult) {
            assertEquals(failureMsg, book, hashMapIterator.next());
          }
        }
      } else {
        assertEquals(
                failureMsg, bookCount * 2, bookByTitleQueryResult.size());
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(0), bookList.get(0));
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(1), bookList.get(0));
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(2), bookList.get(2));
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(3), bookList.get(2));
      }

      bookByTitleQueryResult = titleOrderSet.values(true);
      assertEquals(failureMsg, bookCount, bookByTitleQueryResult.size());
      if (runType == 3) {
        assertEquals(
                failureMsg, bookCount, bookByTitleQueryResult.size());
        if (bookCount == bookByTitleQueryResult.size()) {
          Iterator<Book> hashMapIterator
                  = nonCompTitleHashCodeMap.values().iterator();
          for (Book book : bookByTitleQueryResult) {
            assertEquals(failureMsg, book, hashMapIterator.next());
          }
        }
      } else {
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(0), bookList.get(0));
        assertEquals(
                failureMsg, bookByTitleQueryResult.get(1), bookList.get(2));
      }

      List<Book> booksOfSpecificGenreQueryResult
              = genreOrderSet.values(bookList.get(2).getGenres().get(0));
      assertEquals(failureMsg, 1, booksOfSpecificGenreQueryResult.size());
      assertEquals(failureMsg, bookList.get(2),
              booksOfSpecificGenreQueryResult.get(0));

      List<Book> booksOfSpecificAuthorQueryResult
              = authorOrderSet.values(bookList.get(0).getAuthors().get(0));
      assertEquals(failureMsg, 1, booksOfSpecificAuthorQueryResult.size());
      assertEquals(failureMsg, bookList.get(0),
              booksOfSpecificAuthorQueryResult.get(0));

      booksOfSpecificAuthorQueryResult
              = authorFirstNameOrderSet.values(
                      bookList.get(0).getAuthors().get(0));
      assertEquals(failureMsg, 1, booksOfSpecificAuthorQueryResult.size());
      assertEquals(failureMsg, bookList.get(0),
              booksOfSpecificAuthorQueryResult.get(0));

      List<Book> booksOfSpecificSubtitleQueryResult;
      if (runType != 3) {
        booksOfSpecificSubtitleQueryResult
                = titleOrderSet.values(bookList.get(2).getSubTitle());
        assertEquals(failureMsg, 1, booksOfSpecificSubtitleQueryResult.size());
        assertEquals(failureMsg, bookList.get(2),
                booksOfSpecificSubtitleQueryResult.get(0));
      }

      // remove book and assure indexes properly cleared of values
      failureMsg = "FAILURE in removal of an object from an "
              + "initially empty OrderedSet based on " + componentBasis;

      bookOrderSet.add(bookList.get(5));
      bookOrderSet.remove(bookList.get(2));
      assertEquals(failureMsg, 2, bookOrderSet.size());
      titleOrderSet.add(bookList.get(5));
      titleOrderSet.remove(bookList.get(2));
      assertEquals(failureMsg, 2, titleOrderSet.size());
      authorOrderSet.add(bookList.get(5));
      authorOrderSet.remove(bookList.get(2));
      assertEquals(failureMsg, 2, authorOrderSet.size());
      authorFirstNameOrderSet.add(bookList.get(5));
      authorFirstNameOrderSet.remove(bookList.get(2));
      assertEquals(failureMsg, 2, authorFirstNameOrderSet.size());
      genreOrderSet.add(bookList.get(5));
      genreOrderSet.remove(bookList.get(2));
      assertEquals(failureMsg, 2, genreOrderSet.size());

      bookQueryResult = bookOrderSet.values();
      assertEquals(failureMsg, 2, bookQueryResult.size());
      bookIndex = 0;
      for (Book retrievedBook : bookQueryResult) {
        switch (bookIndex) {
          case 0:
            assertEquals(failureMsg, bookList.get(0), retrievedBook);
            break;
          case 1:
            assertEquals(failureMsg, bookList.get(5), retrievedBook);
            break;
        }
        bookIndex++;
      }

      booksOfSpecificGenreQueryResult
              = genreOrderSet.values(bookList.get(2).getGenres().get(0));
      assertEquals(failureMsg, 0, booksOfSpecificGenreQueryResult.size());

      booksOfSpecificAuthorQueryResult
              = authorOrderSet.values(bookList.get(2).getAuthors().get(0));
      assertEquals(failureMsg, 0, booksOfSpecificAuthorQueryResult.size());

      booksOfSpecificAuthorQueryResult
              = authorFirstNameOrderSet.values(bookList.get(2).getAuthors().get(0));
      assertEquals(failureMsg, 0, booksOfSpecificAuthorQueryResult.size());

      if (runType != 3) {
        booksOfSpecificSubtitleQueryResult
                = titleOrderSet.values(bookList.get(2).getSubTitle());
        assertEquals(failureMsg, 0, booksOfSpecificSubtitleQueryResult.size());
      }

      authorQueryResult
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT);
      assertTrue(failureMsg,
              !authorQueryResult.contains(
                      bookList.get(2).getAuthors().get(0)));

      authorQueryResult
              = authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT);
      assertTrue(failureMsg,
              !authorQueryResult.contains(
                      bookList.get(2).getAuthors().get(0)));

      titleQueryResult
              = titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT);
      assertTrue(failureMsg,
              !titleQueryResult.contains(bookList.get(2).getTitle()));
      assertTrue(failureMsg,
              !titleQueryResult.contains(bookList.get(2).getSubTitle()));

      genreQueryResult
              = genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT);
      assertTrue(failureMsg,
              !genreQueryResult.contains(bookList.get(2).getGenres().get(0)));
      assertTrue(failureMsg,
              genreQueryResult.contains(bookList.get(2).getGenres().get(1)));

      // clear and assure indexes properly cleared of values
      failureMsg = "FAILURE in clearing of an initially empty OrderedSet "
              + "based on " + componentBasis;

      bookOrderSet.clear();
      assertEquals(failureMsg, 0, bookOrderSet.size());
      assertEquals(failureMsg, 0, bookOrderSet.values().size());
      titleOrderSet.clear();
      assertEquals(failureMsg, 0, titleOrderSet.size());
      assertEquals(failureMsg, 0, titleOrderSet.values().size());
      authorOrderSet.clear();
      assertEquals(failureMsg, 0, authorOrderSet.size());
      assertEquals(failureMsg, 0, authorOrderSet.values().size());
      authorFirstNameOrderSet.clear();
      assertEquals(failureMsg, 0, authorFirstNameOrderSet.size());
      assertEquals(failureMsg, 0, authorFirstNameOrderSet.values().size());
      genreOrderSet.clear();
      assertEquals(failureMsg, 0, genreOrderSet.size());
      assertEquals(failureMsg, 0, genreOrderSet.values().size());

      authorQueryResult
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT);
      assertEquals(failureMsg, 0, authorQueryResult.size());

      authorQueryResult
              = authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT);
      assertEquals(failureMsg, 0, authorQueryResult.size());

      titleQueryResult
              = titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT);
      assertEquals(failureMsg, 0, titleQueryResult.size());

      genreQueryResult
              = genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT);
      assertEquals(failureMsg, 0, genreQueryResult.size());

      for (Book book : bookOrderSet) {
        assertEquals(failureMsg, 0, 0); // should never be reached
      }
    }
  }

  /**
   * Test OrderedSet constructed with Book list submitted to constructor.
   */
  @Test
  public void testInitiallyPopulatedSetConstructor() {
    testInitiallyPopulatedSetConstructor(false);
  }

  @Test
  public void testInitiallyPopulatedSetWithSubclass() {
    testInitiallyPopulatedSetConstructor(true);
  }

  private void testInitiallyPopulatedSetConstructor(boolean useSubclass) {
    for (int runType = 1; runType <= 2; runType++) {
      constructKeyComponentProfiles(runType);

      List<Book> bookList = getRandomOrderBookList(useSubclass);

      bookOrderSet = new OrderedSet<Book>(bookList, BOOK_KEY_COMPONENT);
      titleOrderSet = new OrderedSet<Book>(bookList, TITLE_KEY_COMPONENT);
      authorOrderSet = new OrderedSet<Book>(bookList,
              AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      authorFirstNameOrderSet = new OrderedSet<Book>(bookList,
              AUTHOR_FIRST_NAME_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      genreOrderSet = new OrderedSet<Book>(bookList,
              GENRE_KEY_COMPONENT, AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);
//        printKeyComponentList
//            ("Printing compositeKeys for genreOrderSet", genreOrderSet);
//        printEntrySet("Printing entrySet for genreOrderSet", genreOrderSet);
      failureMsg = "Failure in constructor invocation for fully populated "
              + "OrderedSet based on " + componentBasis;
      assertEquals(failureMsg, bookList.size(), bookOrderSet.size());
      assertEquals(failureMsg, bookList.size(), titleOrderSet.size());
      assertEquals(failureMsg, bookList.size(), authorOrderSet.size());
      assertEquals(failureMsg, bookList.size(), authorFirstNameOrderSet.size());
      assertEquals(failureMsg, bookList.size(), genreOrderSet.size());

      Iterator<Book> bookIterator = bookOrderSet.values().iterator();
      Set<Book> bookTreeSet = new TreeSet<Book>(bookList);
      for (Book book : bookTreeSet) {
        assertEquals(failureMsg, book, bookIterator.next());
      }

      Iterator<Object> attributeIterator;

      // Confirm that Title attribute indexing is correct
      Set<Book.Title> titleTreeSet = new TreeSet<Book.Title>();
      for (Book book : bookList) {
        if (book.getTitle() != null) {
          titleTreeSet.add(book.getTitle());
        }
        if (book.getSubTitle() != null) {
          titleTreeSet.add(book.getSubTitle());
        }
      }
      assertEquals(failureMsg, titleTreeSet.size(),
              titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT).size());
      attributeIterator
              = titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT).iterator();
      for (Book.Title title : titleTreeSet) {
        assertEquals(failureMsg, title, attributeIterator.next());
      }

      // Confirm that Author attribute indexing is correct
      Set<Author> authorTreeSet = new TreeSet<Author>();
      for (Book book : bookList) {
        if (book.getAuthors() != null) {
          authorTreeSet.addAll(book.getAuthors());
        }
      }
      assertEquals(failureMsg, authorTreeSet.size(),
              authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT).size());
      attributeIterator
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT).iterator();
      for (Author author : authorTreeSet) {
        assertEquals(failureMsg, author, attributeIterator.next());
      }

      // Confirm that Author by first name attribute indexing is correct
      authorTreeSet = new TreeSet<Author>(new FirstNameComparator());
      for (Book book : bookList) {
        if (book.getAuthors() != null) {
          authorTreeSet.addAll(book.getAuthors());
        }
      }
      assertEquals(failureMsg, authorTreeSet.size(),
              authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT).size());
      attributeIterator
              = authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT).iterator();
      for (Author author : authorTreeSet) {
        assertEquals(failureMsg, author, attributeIterator.next());
      }

      // Confirm that Genre attribute indexing is correct
      Set<Genre> genreTreeSet = new TreeSet<Genre>();
      for (Book book : bookList) {
        if (book.getGenres() != null) {
          genreTreeSet.addAll(book.getGenres());
        }
      }
      assertEquals(failureMsg, genreTreeSet.size(),
              genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT).size());
      attributeIterator
              = genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT).iterator();
      for (Genre genre : genreTreeSet) {
        assertEquals(failureMsg, genre, attributeIterator.next());
      }

      // Confirm that book indexing by Author is accurate
      int authorIndexEntryCount = 0;
      for (Book book : bookList) {
        int titleCount = 0;
        if (book.getTitle() != null) {
          titleCount++;
        }
        if (book.getSubTitle() != null) {
          titleCount++;
        }
        authorIndexEntryCount += book.getAuthors().size() * titleCount;
      }
      // verify authorOrderSet
      List<Book> booksByAuthor = authorOrderSet.values();
      assertEquals(failureMsg, authorIndexEntryCount, booksByAuthor.size());

      attributeIterator
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT).iterator();
      Object currentAuthor = attributeIterator.next();
      for (Book book : booksByAuthor) {
        while (!book.getAuthors().contains(currentAuthor)) {
          currentAuthor = attributeIterator.next();
        }
        assertTrue(failureMsg, (book.getAuthors().contains(currentAuthor)));
      }

      // verify authorFirstNameOrderSet
      List<Book> booksByAuthorFirstName = authorFirstNameOrderSet.values();
      assertEquals(failureMsg, authorIndexEntryCount, booksByAuthorFirstName.size());

      attributeIterator
              = authorFirstNameOrderSet.
                      keyComponentSet(AUTHOR_FIRST_NAME_KEY_COMPONENT).iterator();
      currentAuthor = attributeIterator.next();
      for (Book book : booksByAuthorFirstName) {
        while (!book.getAuthors().contains(currentAuthor)) {
          currentAuthor = attributeIterator.next();
        }
        assertTrue(failureMsg, (book.getAuthors().contains(currentAuthor)));
      }

      // Confirm that book indexing by Title is accurate
      int titleIndexEntryCount = 0;
      for (Book book : bookList) {
        if (book.getTitle() != null) {
          titleIndexEntryCount++;
        }
        if (book.getSubTitle() != null) {
          titleIndexEntryCount++;
        }
      }
      List<Book> booksByTitle = titleOrderSet.values();
      assertEquals(failureMsg, titleIndexEntryCount, booksByTitle.size());

      attributeIterator
              = titleOrderSet.keyComponentSet(TITLE_KEY_COMPONENT).iterator();
      Object currentTitle = attributeIterator.next();
      for (Book book : booksByTitle) {
        while (!book.getTitle().equals(currentTitle)
                && (book.getSubTitle() == null
                || !book.getSubTitle().equals(currentTitle))) {
          currentTitle = attributeIterator.next();
        }
        assertTrue(failureMsg, (book.getTitle().equals(currentTitle)
                || book.getSubTitle().equals(currentTitle)));
      }

      // Confirm that book indexing by Genre is accurate
      int genreIndexEntryCount = 0;
      for (Book book : bookList) {
        int titleCount = 0;
        if (book.getTitle() != null) {
          titleCount++;
        }
        if (book.getSubTitle() != null) {
          titleCount++;
        }
        genreIndexEntryCount
                += book.getAuthors().size() * book.getGenres().size()
                * titleCount;
      }
      List<Book> booksByGenre = genreOrderSet.values();
      assertEquals(failureMsg, genreIndexEntryCount, booksByGenre.size());

      attributeIterator
              = genreOrderSet.keyComponentSet(GENRE_KEY_COMPONENT).iterator();
      Object currentGenre = attributeIterator.next();
      for (Book book : booksByGenre) {
        while (!book.getGenres().contains(currentGenre)) {
          currentGenre = attributeIterator.next();
        }
        assertTrue(failureMsg, book.getGenres().contains(currentGenre));
      }

      // test getting books of specific Author
      Author anAuthor = bookList.get(7).getAuthors().get(0);
      List<Book> booksByAnAuthor = authorOrderSet.values(anAuthor);
      for (Book book : booksByAnAuthor) {
        assertTrue(failureMsg, book.getAuthors().contains(anAuthor));
      }
      List<Book> booksNotByAnAuthor = new ArrayList<Book>(bookList);
      booksNotByAnAuthor.removeAll(booksByAnAuthor);
      for (Book book : booksNotByAnAuthor) {
        assertTrue(failureMsg, !book.getAuthors().contains(anAuthor));
      }
      assertEquals(failureMsg, bookList.size(),
              booksByAnAuthor.size() + booksNotByAnAuthor.size());

      // test getting books of specific Author from first-name-order set
      anAuthor = bookList.get(7).getAuthors().get(0);
      booksByAnAuthor = authorFirstNameOrderSet.values(anAuthor);
      for (Book book : booksByAnAuthor) {
        assertTrue(failureMsg, book.getAuthors().contains(anAuthor));
      }
      booksNotByAnAuthor = new ArrayList<Book>(bookList);
      booksNotByAnAuthor.removeAll(booksByAnAuthor);
      for (Book book : booksNotByAnAuthor) {
        assertTrue(failureMsg, !book.getAuthors().contains(anAuthor));
      }
      assertEquals(failureMsg, bookList.size(),
              booksByAnAuthor.size() + booksNotByAnAuthor.size());

      // test getting books of specific Genre
      Genre aGenre = bookList.get(7).getGenres().get(0);
      List<Book> booksOfAGenre = genreOrderSet.values(aGenre);
      for (Book book : booksOfAGenre) {
        assertTrue(failureMsg, book.getGenres().contains(aGenre));
      }
      List<Book> booksNotOfAGenre = new ArrayList<Book>(bookList);
      booksNotOfAGenre.removeAll(booksOfAGenre);
      for (Book book : booksNotOfAGenre) {
        assertTrue(failureMsg, !book.getGenres().contains(aGenre));
      }
      assertEquals(failureMsg, bookList.size(),
              booksOfAGenre.size() + booksNotOfAGenre.size());

      // Test getting books of specific Title/Subtitle
      Book.Title aTitle = bookList.get(3).getSubTitle();
      List<Book> booksWithATitle = titleOrderSet.values(aTitle);
      for (Book book : booksWithATitle) {
        boolean titleMatches = false;
        if (book.getTitle().equals(aTitle)) {
          titleMatches = true;
        } else if (book.getSubTitle() != null
                && book.getSubTitle().equals(aTitle)) {
          titleMatches = true;
        }
        assertTrue(failureMsg, titleMatches);
      }

      // Test suppression of consecutive duplicates
      List<Book> booksByAuthorNoDups = authorOrderSet.values(true);
      attributeIterator
              = authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT).iterator();
      currentAuthor = attributeIterator.next();
      Book previousBook = null;
      for (Book book : booksByAuthorNoDups) {
        if (previousBook != null) {
          assertTrue(failureMsg, !book.equals(previousBook));
        }
        previousBook = book;

        while (!book.getAuthors().contains(currentAuthor)) {
          currentAuthor = attributeIterator.next();
        }
        assertTrue(failureMsg, (book.getAuthors().contains(currentAuthor)));
      }

      // Test suppression of consecutive duplicates w/ authors by first-name
      booksByAuthorNoDups = authorFirstNameOrderSet.values(true);
      attributeIterator
              = authorFirstNameOrderSet.keyComponentSet(
                      AUTHOR_FIRST_NAME_KEY_COMPONENT).iterator();
      currentAuthor = attributeIterator.next();
      previousBook = null;
      for (Book book : booksByAuthorNoDups) {
        if (previousBook != null) {
          assertTrue(failureMsg, !book.equals(previousBook));
        }
        previousBook = book;

        while (!book.getAuthors().contains(currentAuthor)) {
          currentAuthor = attributeIterator.next();
        }
        assertTrue(failureMsg, book.getAuthors().contains(currentAuthor));
      }
      verifyEntrySet(bookOrderSet);
      verifyEntrySet(titleOrderSet);
      verifyEntrySet(authorOrderSet);
      verifyEntrySet(authorFirstNameOrderSet);
      verifyEntrySet(genreOrderSet);

      // test HashSet equivalency
      failureMsg = "Failure in HashSet equivalency testing for fully populated "
              + "OrderedSet based on " + componentBasis;
      HashSet hashSetEquivalent = new HashSet(bookList);
      assertTrue(failureMsg, hashSetEquivalent.equals(bookOrderSet));
      assertTrue(failureMsg, hashSetEquivalent.equals(titleOrderSet));
      assertTrue(failureMsg, hashSetEquivalent.equals(authorOrderSet));
      assertTrue(failureMsg, hashSetEquivalent.equals(authorFirstNameOrderSet));
      assertTrue(failureMsg, hashSetEquivalent.equals(genreOrderSet));

      testRemoveMethods(bookOrderSet);
      testRemoveMethods(titleOrderSet);
      testRemoveMethods(authorOrderSet);
      testRemoveMethods(authorFirstNameOrderSet);
      testRemoveMethods(genreOrderSet);

      // test remove/clone/modify/add of a value
      authorOrderSet = new OrderedSet<Book>(bookList,
              AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      Book bookToRemove = authorOrderSet.values().get(4);
      assertTrue(failureMsg, authorOrderSet.remove(bookToRemove));
      assertTrue(failureMsg, !authorOrderSet.contains(bookToRemove));
      for (Book book : authorOrderSet.values()) {
        assertTrue(failureMsg, !book.equals(bookToRemove));
      }
      verifyEntrySet(authorOrderSet);

      Book bookToModify = bookToRemove.clone(); // deep copy clone
      List<Author> authorsToModify = bookToModify.getAuthors();
      authorsToModify.get(0).setName("Doe", "Jane");
      authorOrderSet.add(bookToModify);
      assertTrue(failureMsg, authorOrderSet.contains(bookToModify));
      boolean bookInValuesList = false;
      for (Book book : authorOrderSet.values()) {
        if (book.equals(bookToModify)) {
          bookInValuesList = true;
          break;
        }
      }
      assertTrue(failureMsg, bookInValuesList);
      assertTrue(failureMsg,
              authorOrderSet.keyComponentSet(AUTHOR_KEY_COMPONENT).
                      contains(authorsToModify.get(0)));
    }
  }

  private void testRemoveMethods(OrderedSet<Book> orderedSet) {
    Book bookToRemove = orderedSet.values().get(4);
    assertTrue(failureMsg, orderedSet.remove(bookToRemove));
    assertTrue(failureMsg, !orderedSet.contains(bookToRemove));
    for (Book book : orderedSet.values()) {
      assertTrue(failureMsg, !book.equals(bookToRemove));
    }
    verifyEntrySet(orderedSet);

    List<Book> bookListToRemove = new ArrayList<Book>();
    bookListToRemove.add(orderedSet.values().get(2));
    bookListToRemove.add(orderedSet.values().get(6));
    assertTrue(failureMsg, orderedSet.removeAll(bookListToRemove));
    assertTrue(failureMsg, !orderedSet.contains(bookListToRemove.get(0)));
    assertTrue(failureMsg, !orderedSet.contains(bookListToRemove.get(1)));
    for (Book book : orderedSet.values()) {
      assertTrue(failureMsg, !book.equals(bookListToRemove.get(0)));
      assertTrue(failureMsg, !book.equals(bookListToRemove.get(1)));
    }
    verifyEntrySet(orderedSet);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAddOfNullValue() {
      constructKeyComponentProfiles(1);
      authorOrderSet = new OrderedSet<Book>(getRandomOrderBookList(false),
              AUTHOR_KEY_COMPONENT, TITLE_KEY_COMPONENT);
      authorOrderSet.add(null);
  }

  private void verifyEntrySet (OrderedSet<Book> orderedSet) {
    failureMsg = "FAILURE in OrderedSet #entrySet validation";
//    printEntrySet(orderedSet);
    Iterator<Object> previousIterator = null;
    for (Map.Entry<List<Object>,Book> entry : orderedSet.entrySet()) {
      if (previousIterator == null) {
        previousIterator = entry.getKey().iterator();
        continue;
      }
      Iterator<KeyComponentProfile<Book>> keyComponentProfileIterator =
              orderedSet.getKeyComponentProfiles().iterator();
      Iterator<Object> currentIterator = entry.getKey().iterator();
      while (currentIterator.hasNext()) {
        Object previousKeyComponent = previousIterator.next();
        Object currentKeyComponent = currentIterator.next();
        KeyComponentProfile<Book> currentKeyComponentProfile =
                keyComponentProfileIterator.next();
        Comparator currentComparator =
                currentKeyComponentProfile.getKeyComponentClassComparator();
        int compareInt;
        if (currentComparator != null) {
          compareInt = currentComparator.compare(
                  previousKeyComponent, currentKeyComponent);
       } else if (Comparable.class.isAssignableFrom(
                currentKeyComponentProfile.getKeyComponentClass())
               // Book.class implements Comparable<Book>
               || currentKeyComponentProfile.getKeyComponentBasis().equals(
                       KeyComponentProfile.KeyComponentBasis.IDENTITY)) {
          compareInt = ((Comparable)previousKeyComponent).
                  compareTo(currentKeyComponent);
        } else {
          compareInt = previousKeyComponent.hashCode() -
                  currentKeyComponent.hashCode();
        }
        if (compareInt == 0) {
          continue;
        }
        assertTrue(failureMsg, compareInt < 0);
        break;
      }
      previousIterator = entry.getKey().iterator();
    }
  }

  private void printEntrySet (OrderedSet<Book> orderedSet) {
    StringBuilder orderString = new StringBuilder();
    // build orderString and print title
    int counter = 0;
    for (KeyComponentProfile keyComponentProfile : orderedSet.getKeyComponentProfiles()) {
      if (keyComponentProfile.getKeyComponentClass().equals(Object.class)) {
        continue;
      }
      if (++counter > 1) {
        orderString.append(BAR);
      }
      orderString.append(keyComponentProfile.getKeyComponentClass().getSimpleName());
      if (keyComponentProfile.getKeyComponentClassComparator() != null) {
        orderString.append('(').append(keyComponentProfile.
                getKeyComponentClassComparator().getClass().getSimpleName()).
                append(')');
      }
    }
    System.out.println("\n*** OrderedKey entries listed in " +
            orderString + " order ***");

    // print column headers using keyComponentClass names in uppercase
    printBorderLine(orderedSet.getKeyComponentProfiles().size());
    System.out.print(BAR);
    for (KeyComponentProfile keyComponentProfile : orderedSet.getKeyComponentProfiles()) {
      if (keyComponentProfile.getKeyComponentClass().equals(Object.class)) {
        printCell(Book.class.getSimpleName().toUpperCase());
      } else {
        printCell(keyComponentProfile.getKeyComponentClass().
                getSimpleName().toUpperCase());
      }
    }
    System.out.print("\n");

    // print individual entries in cell format (truncated to CELL_WIDTH)
    for (Map.Entry<List<Object>,Book> entry : orderedSet.entrySet()) {
      List<Object> attributeList = entry.getKey();
      printBorderLine(orderedSet.getKeyComponentProfiles().size());
      System.out.print(BAR);
      for (Object attribute : attributeList) {
        printCell(attribute);
      }
      System.out.print("\n");
    }
    printBorderLine(orderedSet.getKeyComponentProfiles().size());
  }

  private void printCell(Object cellContent) {
    System.out.print(String.format("%1$-" + CELL_WIDTH + "s",
            (cellContent.toString().length() > CELL_WIDTH ?
                    cellContent.toString().substring(0, CELL_WIDTH) :
                    cellContent.toString())));
    System.out.print(BAR);
  }

  private void printBorderLine(int attributeListSize) {
    System.out.print(BAR);
    for (int i = 0 ; i < attributeListSize; i++) {
      System.out.print(CELL_BORDER);
      System.out.print(BAR);
    }
    System.out.print("\n");
  }

  private List<Book> getRandomOrderBookList(boolean useSubclass) {
    List<Book> bookList = new ArrayList<Book>() {
      {
        add(new Book("Adventures of Huckleberry Finn",
                "Life on the Mississippi",
                Arrays.asList(new Genre("Fiction"), new Genre("Adventure")),
                Arrays.asList(new Author("Twain", "Mark"))));
        add(new Book("Merriam-Webster Dictionary", null,
                Arrays.asList(new Genre("Nonfiction"),
                        new Genre("Reference")),
                Arrays.asList(new Author("Webster", "Noah"),
                        new Author("Merriam", "George"))));
        add(new Book("Through the Brazilian Wilderness", "vvv",
                Arrays.asList(new Genre("Nonfiction"),
                        new Genre("Adventure")),
                Arrays.asList(new Author("Roosevelt", "Theodore"))));
        add(new Book("Advice to Youth", "vvvv",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Twain", "Mark"))));
        add(new Book("Lucifer's Hammer", "vvvv",
                Arrays.asList(new Genre("Fiction"),
                        new Genre("Science Fiction"),
                        new Genre("Adventure")),
                Arrays.asList(new Author("Niven", "Larry"),
                        new Author("Pournelle", "Jerry"))));
        add(new Book("Slaughterhouse-Five", "vvvvv",
                Arrays.asList(new Genre("Fiction"),
                        new Genre("Science Fiction"),
                        new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt"))));
        add(new Book("Dissertation on the English Language", "vvvvvv",
                Arrays.asList(new Genre("Nonfiction"),
                        new Genre("Essay")),
                Arrays.asList(new Author("Webster", "Noah"))));
        add(new Book("Man Without a Country", "vvvvvv",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt"))));
      }
    };
    if (useSubclass) {
      List<Book> bookletList = new ArrayList<Book>();
      for (Book book : bookList) {
        bookletList.add(new Booklet(book));
      }
      return bookletList;
    } else {
      return bookList;
    }
  }

  private void constructKeyComponentProfiles(int runType) {

    BOOK_KEY_COMPONENT = new KeyComponentProfile<Book>(Book.class, Book.class);
    switch (runType) {
      case 1:
        componentBasis = "METHOD";
        TITLE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(
                        Book.class, Book.Title.class,
                        TITLE_GET_METHOD, SUBTITLE_GET_METHOD);
        AUTHOR_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class,
                        AUTHOR_GET_METHOD);
        AUTHOR_FIRST_NAME_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class,
                        new FirstNameComparator(), AUTHOR_GET_METHOD);
        GENRE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Genre.class,
                        GENRE_GET_METHOD);
        break;
      case 2:
        componentBasis = "CLASS";
        TITLE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Book.Title.class);
        AUTHOR_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class);
        AUTHOR_FIRST_NAME_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class,
                        new FirstNameComparator());
        GENRE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Genre.class);
        break;
      case 3:
        componentBasis = "CLASS/nonComparableTitle";
        TITLE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(
                        Book.class, Book.NoncomparableTitle.class);
        AUTHOR_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class);
        AUTHOR_FIRST_NAME_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Author.class,
                        new FirstNameComparator());
        GENRE_KEY_COMPONENT
                = new KeyComponentProfile<Book>(Book.class, Genre.class);
        break;
    }
  }

  public void printBooksOfAnAuthor(Author author) {
    for (Book book : authorOrderSet.values(author)) {
      System.out.println(book);
    }
  }

  private void printKeyListSet(String title, OrderedSet<Book> set) {
    System.out.println("\n===============================");
    System.out.println(title + "  NBR OF keyLists = "
            + set.compositeKeys().size());
    System.out.println("===============================");
    for (List<Object> keyList : set.compositeKeys()) {
      printKeyComponentList(keyList);
    }
  }

  private void printKeyComponentList(List<Object> keyComponentList) {
    System.out.println("**Key List for this entry**");
    int objectCount = 0;
    for (Object keyComponent : keyComponentList) {
      System.out.println("  keyComponent  #" + ++objectCount + " class: "
              + keyComponent.getClass().getSimpleName());
      System.out.println("      " + keyComponent);
    }
  }

  private void printEntrySet(
          String title, OrderedSet<Book> set) {
    System.out.println("\n===============================");
    System.out.println(title + "  NBR OF entries = " + set.entrySet().size());
    System.out.println("===============================");
    for (Map.Entry<List<Object>, Book> entry : set.entrySet()) {
      System.out.println("\n**Value for this entry**");
      System.out.println("  " + entry.getValue());
      printKeyComponentList(entry.getKey());
    }
  }

  class FirstNameComparator implements Comparator<Author> {

    @Override
    public int compare(Author o1, Author o2) {
      return o1.firstName.compareTo(o2.firstName);
    }
  }

  class HashCodeComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      return o1.hashCode() - o2.hashCode();
    }
  }
}
