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

import java.util.ArrayList;
import java.util.List;

/**
 * Book class for testing purposes
 *
 * @author Daniel Vimont
 */
class Book implements Comparable<Book>, Cloneable {

    static int bookIdGenerator = 0;
    int bookId;
    Title title;
    NoncomparableTitle nonCompTitle;
    Title subTitle;
    List<Genre> genres;
    List<Author> authors;
    InaccessibleAttribute unindexableObject;

    public Book(String titleString, String subTitleString, List<Genre> genres,
            List<Author> authors) {
        this.bookId = ++bookIdGenerator;
        if (titleString != null) {
            this.title = new Title(titleString);
            this.nonCompTitle = new NoncomparableTitle(titleString);
        }
        if (subTitleString != null) {
            this.subTitle = new Title(subTitleString);
        }
        this.genres = genres;
        this.authors = authors;
        this.unindexableObject = new InaccessibleAttribute();
    }

    public Title getTitle() {
        return title;
    }

    public Title getSubTitle() {
        return subTitle;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public NoncomparableTitle getNoncomparableTitle() {
      return nonCompTitle;
    }

    @Override
    public int compareTo(Book other) {
      if (other == null) {
          return 1;
      }
      return (new Integer(bookId)).compareTo(other.bookId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null
                || !this.getClass().equals(other.getClass())) {
            return false;
        }
        return compareTo((Book)other) == 0;
    }

    @Override
    public String toString() {
        final String INDENT = "     ";
        StringBuilder output = new StringBuilder();
        output.append("TITLE: ").append(title).append(" ; ");
        for (Author author : authors) {
            output.append(INDENT).append("AUTHOR: ").append(author).append(" ; ");
        }
        for (Genre genre : genres) {
            output.append(INDENT).append("GENRE: ").append(genre).append(" ; ");
        }
        return output.toString();
    }

    @Override
    public Book clone() {
      List<Genre> clonedGenres = new ArrayList<Genre>();
      List<Author> clonedAuthors = new ArrayList<Author>();
      for (Genre genre : genres) {
        clonedGenres.add(genre.clone());
      }
      for (Author author : authors) {
        clonedAuthors.add(author.clone());
      }
      Book clonedBook = new Book(
              title.title, subTitle.title, clonedGenres, clonedAuthors);
      clonedBook.bookId = this.bookId;
      return clonedBook;
    }

    public class Title implements Comparable<Title> {

        String title;

        public Title(String titleString) {
            this.title = titleString;
        }

        @Override
        public int compareTo(Title other) {
            if (other == null) {
                return 1;
            }
            if (this.title == null ^ other.title == null) {
                return (this.title == null) ? -1 : 1;
            }
            if (this.title == null && other.title == null) {
                return 0;
            }
            return this.title.toLowerCase().compareTo(other.title.toLowerCase());
        }

        @Override
        public boolean equals(Object other) {
            if (other == null
                    || !this.getClass().isAssignableFrom(other.getClass())) {
                return false;
            }
            return compareTo((Title)other) == 0;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public class NoncomparableTitle {
         String title;

        public NoncomparableTitle(String titleString) {
            this.title = titleString;
        }
   }
}
