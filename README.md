# OrderedSet
An *OrderedSet* extends the standard Java collections framework to provide composite-key based ordering of a set of values (somewhat analogous to <a href="https://www.techopedia.com/definition/6572/composite-key" target="_blank">composite key ordering in a relational database</a>). Its unique advantage (over standard ordering/sorting options such as TreeSet, SortedSet, or Collections#sort) is in **automatic ordering based on collection-attributes**, such as in a Book class that has an *authors* (set of Author objects) attribute and a *genres* (set of Genre objects) attribute , which could be ordered via a *Genre|Author|Title* composite-key. The full results of ordering are provided by the *OrderedSet#entrySet* method which returns ordered entries, each consisting of a composite-key mapped to its corresponding value.

*OrderedSet* is compatible with **Java 1.6 and later**.

Complete end-user Javadocs documentation is viewable here: http://bit.ly/ordered-set

A *Gist* containing several complete usage examples is available here: http://bit.ly/ordered-set-gist

---
#### NEWSFLASH: April 8, 2016
#####OrderedSet is now available via the Maven Central Repository: http://bit.ly/ordered-set-mvn-repo

---
#### Overview
The most straightforward way to understand *OrderedSet* functionality is through usage examples.

Given a class, **Book**, with attributes (or collections of attributes) accessible via the methods **#getAuthors()**, **#getTitle()**, **#getGenres()**, and **#getPublicationDate()**, an *OrderedSet* may be constructed to automatically order a collection of **Book** objects via an **Author|Title|Book** composite-key, another <i>OrderedSet</i> constructed to order the **Book**s via a **Genre|Author|Title|Book** composite-key, another constructed with a **PublicationDate|Title|Book** composite-key, etc.

The following examples show *composite-key* ordering of a set of objects of the **Book.class**, which has attributes of **Author**, **Title**, **Genre**, and **Date** classes returned by the methods **Book#getAuthors()**, **Book#getTitle()**, **Book#getGenres()**, **Book#getPublicationDate()**, and **Book#getRevisionPublicationDate()**.

Example 1 orders **Book**s by **Author|Title**; Example 2 orders **Book**s by **Genre|PublicationDate|Title** (with each genre's most recently published works listed first).

```java
  //== EXAMPLE 1 ==//
  public void orderBooksByAuthorAndTitle() {

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByAuthorAndTitle
            = new OrderedSet<>(
                    new KeyComponentProfile<>(Book.class, Author.class),
                    new KeyComponentProfile<>(Book.class, Book.Title.class));

    booksByAuthorAndTitle.addAll(getRandomOrderBookCollection());

    //-- Print results hierarchically --//
    System.out.println("\n========\nBooks ordered by AUTHOR and TITLE\n========");
    printHierarchically(booksByAuthorAndTitle);
  }

  //== EXAMPLE 2 ==//
  public void orderBooksByGenreNewestToOldest() throws NoSuchMethodException {

    //-- Construct KeyComponentProfiles (one w/ Comparator & specific get method specified) --//
    KeyComponentProfile<Book> genreKeyComponent
            = new KeyComponentProfile<>(Book.class, Genre.class);
    KeyComponentProfile<Book> publicationDateKeyComponent
            = new KeyComponentProfile<>(Book.class, Date.class,
                    (date1, date2) -> date2.compareTo(date1), // DESCENDING Date Comparator
                    Book.class.getDeclaredMethod("getPublicationDate")); // limit Date.class focus to THIS method!
    KeyComponentProfile<Book> titleKeyComponent
            = new KeyComponentProfile<>(Book.class, Book.Title.class);

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByGenreAndPublicationDateAndTitle
            = new OrderedSet<>(getRandomOrderBookCollection(),
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
```
See complete Gist examples here: http://bit.ly/ordered-set-gist