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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Simple examples of constructing and querying an OrderedSet.
 */
public class OrderedSetGist {

  public void examplesOfOrderedSetUsage() throws NoSuchMethodException {
    orderBooksByAuthorAndTitle();
    orderBooksByGenreNewestToOldest();
  }

  /**
   * EXAMPLE 1
   */
  public void orderBooksByAuthorAndTitle() {

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByAuthorAndTitle
//            = new OrderedSet<>( // Java 8
            = new OrderedSet<Book>(
                    new KeyComponentProfile<Book>(Book.class, Author.class),
                    new KeyComponentProfile<Book>(Book.class, Book.Title.class));

    booksByAuthorAndTitle.addAll(getRandomOrderBookCollection());

    //-- Print results hierarchically --//
    System.out.println("\n========\nBooks ordered by AUTHOR and TITLE\n========");
    printHierarchically(booksByAuthorAndTitle);

  }

  /**
   * EXAMPLE 2
   */
  public void orderBooksByGenreNewestToOldest() throws NoSuchMethodException {

    //-- Construct KeyComponentProfiles (one w/ Comparator & specific get method specified) --//
    KeyComponentProfile<Book> genreKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Genre.class);
    KeyComponentProfile<Book> publicationDateKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Date.class,
                    // (date1, date2) -> date2.compareTo(date1), // lambda in Java 8
                    new DescendingDateComparator(),
                    Book.class.getDeclaredMethod("getPublicationDate")); // limit Date.class focus to THIS method!
    KeyComponentProfile<Book> titleKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Book.Title.class);

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByGenreAndPublicationDateAndTitle
            = new OrderedSet<Book>(getRandomOrderBookCollection(),
                    genreKeyComponent, publicationDateKeyComponent, titleKeyComponent);

    //-- Print results hierarchically --//
    System.out.println("\n========\nBooks by Genre, listed NEWEST to OLDEST\n========");
    printHierarchically(booksByGenreAndPublicationDateAndTitle);

    //-- Print all Genres in natural order --//
    System.out.println("==========\nGENRE list\n==========");
    for (Object genre : booksByGenreAndPublicationDateAndTitle.keyComponentSet(genreKeyComponent)) {
      System.out.println(genre);
    }
  }

  void printHierarchically(OrderedSet<?> orderedSet) {
    final String TAB = "     ";
    List<Object> previousKeyComponents =
            new ArrayList<Object>(Arrays.asList("", "", "", "", "", "", "", "", "", ""));
    for (Map.Entry<List<Object>,?> entry : orderedSet.entrySet()){
      List<Object> keyComponents = entry.getKey();
      Iterator<Object> keyComponentIterator = keyComponents.iterator();
      Iterator<Object> previousKeyComponentIterator = previousKeyComponents.iterator();
      int tabCount = 0;
      boolean printRemainingComponents = false;
      while (keyComponentIterator.hasNext()) {
        Object keyComponent = keyComponentIterator.next();
        if (!keyComponent.equals(previousKeyComponentIterator.next()) || printRemainingComponents) {
          printRemainingComponents = true;
          for (int i = 0; i < tabCount; i++) {
            System.out.print(TAB);
          }
          if (Date.class.isAssignableFrom(keyComponent.getClass())) {
            System.out.println("DATE PUBLISHED: " + keyComponent);
          } else {
            System.out.println(keyComponent);
          }
        }
        tabCount++;
      }
      previousKeyComponents = keyComponents;
    }
    System.out.println("===============");
  }

  //==========================================================================
  // CONSTRUCT SAMPLE LIST OF BOOKS IN RANDOM ORDER
  //==========================================================================
  private Collection<Book> getRandomOrderBookCollection() {
    return new ArrayList<Book>(Arrays.asList(
        new Book(1, "Adventures of Huckleberry Finn",
                Arrays.asList(new Genre("Fiction"), new Genre("Adventure")),
                Arrays.asList(new Author("Twain", "Mark")),
                "1884-12-10", "1912-01-01"),
        new Book(2, "Merriam-Webster Dictionary",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Reference")),
                Arrays.asList(new Author("Webster", "Noah"),
                        new Author("Merriam", "George")),
                "1840-01-01", "1864-01-01"),
        new Book(3, "Advice to Youth",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Twain", "Mark")),
                "1882-01-01", null),
        new Book(4, "Lucifer's Hammer",
                Arrays.asList(new Genre("Fiction"), new Genre("Science Fiction"),
                        new Genre("Adventure")),
                Arrays.asList(new Author("Niven", "Larry"), new Author("Pournelle", "Jerry")),
                "1977-01-01", "1993-01-01"),
        new Book(5, "Slaughterhouse-Five",
                Arrays.asList(new Genre("Fiction"), new Genre("Science Fiction"),
                        new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt")),
                "1969-03-01", "1972-01-01"),
        new Book(6, "Dissertation on the English Language",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Essay")),
                Arrays.asList(new Author("Webster", "Noah")),
                "1789-01-01", "1793-01-01"),
        new Book(7, "Man Without a Country",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt")),
                "2005-01-01", "2007-01-01")
            ));
  }

  //========================
  // CLASS DEFINITIONS
  //========================
  public class Book implements Comparable<Book> {

    int bookId;
    Title title;
    List<Genre> genres;
    List<Author> authors;
    Date revisionPublicationDate;
    Date publicationDate;
    final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Book(int bookId, String title, List<Genre> genres, List<Author> authors,
            String publicationDate, String revisionPublicationDate) {
      this.bookId = bookId;
      this.title = new Title(title);
      this.genres = genres;
      this.authors = authors;
      try { this.publicationDate = DATE_FORMAT.parse(publicationDate);
      } catch (ParseException e) {}
      if (revisionPublicationDate == null) {
        this.revisionPublicationDate = null;
      } else {
        try { this.revisionPublicationDate = DATE_FORMAT.parse(revisionPublicationDate);
        } catch (ParseException e) {}
      }
    }

    public Title getTitle() {
      return title;
    }

    public List<Genre> getGenres() {
      return genres;
    }

    public List<Author> getAuthors() {
      return authors;
    }

    public Date getPublicationDate() {
      return publicationDate;
    }

    public Date getRevisionPublicationDate() {
      return revisionPublicationDate;
    }

    @Override
    public int compareTo(Book other) {
      return (new Integer(bookId)).compareTo(other.bookId);
    }

    @Override
    public String toString() {
      return "BOOK #" + bookId + " (" + title + ")";
    }

    public class Title implements Comparable<Title> {

      String title;

      public Title(String title) {
        this.title = title;
      }

      @Override
      public int compareTo(Title other) {
        return this.title.toLowerCase().compareTo(other.title.toLowerCase());
      }

      @Override
      public String toString() {
        return "TITLE: " + title;
      }
    }
  }

  public class Author implements Comparable<Author> {

    String lastName, firstName;

    public Author(String lastName, String firstName) {
      this.lastName = lastName;
      this.firstName = firstName;
    }

    @Override
    public int compareTo(Author other) {
      int returnedInt = this.lastName.toLowerCase().compareTo(other.lastName.toLowerCase());
      if (returnedInt == 0) {
        returnedInt = this.firstName.toLowerCase().compareTo(other.firstName.toLowerCase());
      }
      return returnedInt;
    }

    @Override
    public String toString() {
      return "AUTHOR: " + firstName + " " + lastName;
    }
  }

  public class Genre implements Comparable<Genre> {

    String genre;

    public Genre(String genre) {
      this.genre = genre;
    }

    @Override
    public int compareTo(Genre other) {
      return this.genre.toLowerCase().compareTo(other.genre.toLowerCase());
    }

    @Override
    public String toString() {
      return "GENRE: " + genre;
    }
  }

  public class DescendingDateComparator implements Comparator<Date> {

    @Override
    public int compare(Date o1, Date o2) {
      return o2.compareTo(o1);
    }
  }

  public static void main(String[] args) throws Exception {
    new OrderedSetGist().examplesOfOrderedSetUsage();
  }
}
