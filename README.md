# OrderedSet
An <i>OrderedSet</i> provides composite-key based ordering of a set of values (analogous to <a href="https://www.techopedia.com/definition/6572/composite-key" target="_blank">composite key ordering in a relational database</a>).

Complete end-user Javadocs documentation is viewable here: http://bit.ly/ordered-set

The most straightforward way to understand *OrderedSet* functionality is through usage examples. Given a class, **Book**, with attributes (or collections of attributes) accessible via the methods **#getAuthors()**, **#getTitle()**, **#getGenres()**, and **#getPublicationDate()**, an *OrderedSet* may be constructed to automatically order a collection of **Book** objects via an **Author|Title|Book** composite-key, another <i>OrderedSet</i> constructed to order the **Book**s via a **Genre|Author|Title|Book** composite-key, another constructed with a **PublicationDate|Title|Book** composite-key, etc.

The following examples show *composite-key* ordering of a set of objects of the **Book.class**, which has attributes of **Author**, **Title**, **Genre**, and **Date** classes returned by the methods **Book#getAuthors()**, **Book#getTitle()**, **Book#getGenres()**, **Book#getPublicationDate()**, and **Book#getRevisionPublicationDate()**.

Example 1 orders **Book**s by **Title**; Example 2 orders **Book**s by **Author|Title**; Example 3 orders **Book**s by **PublicationDate|Title** (most recently published listed first).

```java
  //== EXAMPLE 1 ==//
  public void orderBooksByTitle() {

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByTitle =
            new OrderedSet<Book>(
                    new KeyComponentProfile<Book>(Book.class, Title.class));
    booksByTitle.addAll(getRandomOrderBookCollection());

    //-- Get ordered list of Books via OrderedSet#values method --//
    System.out.println("Books in TITLE order");
    for (Book book : booksByTitle.values()) {
      System.out.println(book);
    }
  }

  //== EXAMPLE 2 ==//
  public void orderBooksByAuthorAndTitle() {

    //-- Construct KeyComponentProfiles --//
    KeyComponentProfile<Book> titleKeyComponent =
            new KeyComponentProfile<Book>(Book.class, Title.class);
    KeyComponentProfile<Book> authorKeyComponent =
            new KeyComponentProfile<Book>(Book.class, Author.class);

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByAuthorAndTitle =
            new OrderedSet<Book>(authorKeyComponent, titleKeyComponent);
    booksByAuthorAndTitle.addAll(getRandomOrderBookCollection());

    //-- Query OrderedSet via #keyComponentSet(KeyComponentProfile) & #values(Object) methods --//
    System.out.println("Books grouped by AUTHOR, in TITLE order");
    for (Object author : booksByAuthorAndTitle.keyComponentSet(authorKeyComponent)) {
      System.out.println("Books by " + author + "\n------");
      for (Book book : booksByAuthorAndTitle.values(author)) {
        System.out.println(book);
      }
    }
  }

  //== EXAMPLE 3 ==//
  public void orderBooksNewestToOldest() {

    //-- Construct KeyComponentProfiles (one w/ Comparator & specific get-method specified) --//
    KeyComponentProfile<Book> publicationDateKeyComponent =
            new KeyComponentProfile<Book>(Book.class, Date.class,
                    new DescendingDateComparator(),
                    Book.class.getDeclaredMethod("getPublicationDate"));
    KeyComponentProfile<Book> titleKeyComponent =
            new KeyComponentProfile<Book>(Book.class, Title.class);

    //-- Construct and populate OrderedSet --//
    OrderedSet<Book> booksByPublicationDateAndTitle =
            new OrderedSet<Book>(publicationDateKeyComponent, titleKeyComponent);
    booksByPublicationDateAndTitle.addAll(getRandomOrderBookCollection());

    //-- Get ordered list of Books via OrderedSet#values method --//
    System.out.println("Books in PUBLICATION-DATE & TITLE order (newest first)");
    for (Book book : booksByPublicationDateAndTitle.values()) {
      System.out.println(book);
    }
  }
```
