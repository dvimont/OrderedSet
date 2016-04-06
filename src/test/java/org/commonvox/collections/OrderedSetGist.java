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
import java.util.List;

/**
 * Simple examples of constructing and querying an OrderedSet.
 */
public class OrderedSetGist {

  public void examplesOfOrderedSetUsage() throws NoSuchMethodException {
    orderBooksByTitle();
    orderBooksByAuthorAndTitle();
    orderBooksNewestToOldest();
  }

  /**
   * Example 1
   */
  public void orderBooksByTitle() {

    //-- Construct OrderedSet --//
    OrderedSet<Book> booksByTitle
            = new OrderedSet<Book>(
                    new KeyComponentProfile<Book>(Book.class, Book.Title.class));
    booksByTitle.addAll(getRandomOrderBookCollection());

    //-- Query OrderedSet via #values method --//
    System.out.println("\n========\nBooks in TITLE order\n========");
    for (Book book : booksByTitle.values()) {
      System.out.println(book);
    }
  }

  /**
   * Example 2
   */
  public void orderBooksByAuthorAndTitle() {

    //-- Construct KeyComponentProfiles --//
    KeyComponentProfile<Book> titleKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Book.Title.class);
    KeyComponentProfile<Book> authorKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Author.class);

    //-- Construct OrderedSet --//
    OrderedSet<Book> booksByAuthorAndTitle
            = new OrderedSet<Book>(getRandomOrderBookCollection(),
                    authorKeyComponent, titleKeyComponent);

    //-- Query OrderedSet via #keyComponentSet() & #values(Object) methods --//
    System.out.println(
            "\n========\nBooks grouped by AUTHOR, in TITLE order\n========");
    for (Object author : booksByAuthorAndTitle.keyComponentSet(authorKeyComponent)) {
      System.out.println("------\nBooks by " + author + "\n------");
      for (Book book : booksByAuthorAndTitle.values(author)) {
        System.out.println("  " + book);
      }
    }
  }

  /**
   * Example 3
   */
  public void orderBooksNewestToOldest() throws NoSuchMethodException {

    //-- Construct KeyComponentProfiles (one w/ Comparator & specific get method specified) --//
    KeyComponentProfile<Book> publicationDateKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Date.class,
                    new DescendingDateComparator(),
                    Book.class.getDeclaredMethod("getPublicationDate"));
    KeyComponentProfile<Book> titleKeyComponent
            = new KeyComponentProfile<Book>(Book.class, Book.Title.class);

    //-- Construct OrderedSet --//
    OrderedSet<Book> booksByPublicationDateAndTitle
            = new OrderedSet<Book>(getRandomOrderBookCollection(),
                    publicationDateKeyComponent, titleKeyComponent);

    //-- Query OrderedSet via #values method --//
    System.out.println(
            "\n========\nBooks listed NEWEST to OLDEST\n========");
    for (Book book : booksByPublicationDateAndTitle.values()) {
      System.out.println(book);
    }
  }

  //==========================================================================
  // CONSTRUCT SAMPLE LIST OF BOOKS IN RANDOM ORDER
  //==========================================================================
  private Collection<Book> getRandomOrderBookCollection() {
    return new ArrayList<Book>() {
      {
        add(new Book("Adventures of Huckleberry Finn",
                Arrays.asList(new Genre("Fiction"), new Genre("Adventure")),
                Arrays.asList(new Author("Twain", "Mark")),
                "1884-12-10", "1912-01-01"));
        add(new Book("Merriam-Webster Dictionary",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Reference")),
                Arrays.asList(new Author("Webster", "Noah"),
                        new Author("Merriam", "George")),
                "1840-01-01", "1864-01-01"));
        add(new Book("Through the Brazilian Wilderness",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Adventure")),
                Arrays.asList(new Author("Roosevelt", "Theodore")),
                "1914-01-01", null));
        add(new Book("Advice to Youth",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Twain", "Mark")),
                "1882-01-01", null));
        add(new Book("Lucifer's Hammer",
                Arrays.asList(new Genre("Fiction"), new Genre("Science Fiction"),
                        new Genre("Adventure")),
                Arrays.asList(new Author("Niven", "Larry"), new Author("Pournelle", "Jerry")),
                "1977-01-01", "1993-01-01"));
        add(new Book("Slaughterhouse-Five",
                Arrays.asList(new Genre("Fiction"), new Genre("Science Fiction"),
                        new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt")),
                "1969-03-01", "1972-01-01"));
        add(new Book("Dissertation on the English Language",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Essay")),
                Arrays.asList(new Author("Webster", "Noah")),
                "1789-01-01", "1793-01-01"));
        add(new Book("Man Without a Country",
                Arrays.asList(new Genre("Nonfiction"), new Genre("Satire")),
                Arrays.asList(new Author("Vonnegut", "Kurt")),
                "2005-01-01", "2007-01-01"));
      }
    };
  }

  //========================
  // CLASS DEFINITIONS
  //========================
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static int bookIdGenerator = 0;

  public class Book implements Comparable<Book> {

    int bookId;
    Title title;
    List<Genre> genres;
    List<Author> authors;
    Date revisionPublicationDate;
    Date publicationDate;

    public Book(String title, List<Genre> genres, List<Author> authors,
            String publicationDate, String revisionPublicationDate) {
      this.bookId = ++bookIdGenerator;
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
      StringBuilder output = new StringBuilder();
      output.append("TITLE:<").append(title).append("> | ");
      for (Author author : authors) {
        output.append("AUTHOR:<").append(author).append("> | ");
      }
      for (Genre genre : genres) {
        output.append("GENRE:<").append(genre).append("> | ");
      }
      output.append("PUBL:<").append(publicationDate).append(">");
      return output.toString();
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
        return title;
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
      return firstName + " " + lastName;
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
      return genre;
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
