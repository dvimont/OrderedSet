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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * An <i>OrderedSet</i> provides composite-key based ordering of a
 * set of values (analogous to
 * <a href="https://www.techopedia.com/definition/6572/composite-key"
 * target="_blank">composite key ordering in a relational database</a>).
 * For example, given a class, {@code Book}, with attributes (or collections of
 * attributes) accessible via the methods {@code #getAuthors()},
 * {@code #getTitle()}, {@code #getGenres()}, and {@code #getPublicationDate()}
 * an <i>OrderedSet</i> may be constructed to automatically order a collection
 * of {@code Book} objects via an {@code Author|Title|Book} composite-key,
 * another <i>OrderedSet</i> constructed to order the {@code Book}s via a
 * {@code Genre|Author|Title|Book} composite-key, another constructed with a
 * {@code PublicationDate|Title|Book} composite-key, etc.
 * <br><br>
 * <i>Usage examples can be found <b><a href="#usage_examples">here</a></b>.</i>
 * <br><br>
 * The components of an <i>OrderedSet</i>'s composite-key structure are
 * stipulated via an array of {@link KeyComponentProfile} objects submitted to
 * the <i>OrderedSet</i>
 * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
 * constructor}. Continuing with the first example above, to create an
 * <i>OrderedSet</i> for ordering a set of {@code Book} values via an
 * {@code Author|Title|Book} composite-key, the following constructor invocation
 * could be used:
 * <PRE>{@code
  OrderedSet<Book> booksOrderedByAuthorAndTitle =
    new OrderedSet<Book>(
      new KeyComponentProfile<Book>(Book.class, Author.class),   // 1st component is Author.
      new KeyComponentProfile<Book>(Book.class, Title.class));   // 2nd component is Title.
          // 3rd component is Book (automatically added to assure composite-key uniqueness). }</PRE>
 * Note that a {@link KeyComponentProfile} based upon the <i>valueClass</i>
 * itself (in the example case, {@code Book}) is always automatically appended
 * as the final component in any specified {@link KeyComponentProfile} array,
 * to assure uniqueness of composite-keys.<br><br>
 * Any {@code Book} objects {@link #add(java.lang.Object) add}ed to the set
 * above are immediately retrievable as a list in {@code Author|Title|Book}
 * order via invocation of the {@link #values()} method;
 * given that a {@code Book} may have multiple {@code Author}s, a single
 * {@code Book} may appear in the list multiple times (once for each of its
 * {@code Author}s). Additionally, a list of the {@code Book}s by a specific
 * {@code Author} (in {@code Title|Book} order) is retrievable via submission of
 * an {@code Author} object to the {@link #values(java.lang.Object)
 * values(Object keyComponentObject)} method. <b>Note that use of one of the
 * {@link #values() values} methods is required to retrieve an ordered list of
 * values. Iteration against the <i>OrderedSet</i> itself gives exactly the same
 * result as would iteration against an equivalent
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/HashSet.html">
 * HashSet</a> populated with the same collection of objects.</b>
 * <br><br>
 * <hr>
 * <b><a href="#usage_examples">GO TO USAGE EXAMPLES</a></b>
 * <hr>
 * <a name="construct_KeyComponentProfile"></a><br>
 * <b>CONSTRUCTION OF A <i>KeyComponentProfile</i></b><br>
 * The first two (mandatory) parameters of the
 * {@link KeyComponentProfile#KeyComponentProfile(java.lang.Class, java.lang.Class, java.lang.reflect.Method...)
 * KeyComponentProfile constructor} are (1) the <i>valueClass</i> (the class
 * of objects contained in the <i>OrderedSet</i> to which the
 * <i>KeyComponentProfile</i> belongs) and (2) the
 * <i>keyComponentClass</i> (the class of objects
 * to be automatically retrieved and ordered as a component of the
 * composite-key). During construction of a {@link KeyComponentProfile}, Java
 * reflection is used to find all <i>valueClass</i>-methods which return
 * object(s) of the <i>keyComponentClass</i>
 * (e.g., in the example above, {@code #getTitle} returns a {@code Title.class}
 * object, and {@code #getAuthors} returns a collection of {@code Author.class}
 * objects).
 * <br>
 * <a name="order_keyComponents"></a><br>
 * <b>AUTOMATIC RETRIEVAL AND ORDERING OF COMPOSITE-KEY COMPONENTS</b>
 * <br>
 * When a value is added to the <i>OrderedSet</i>, all of its
 * related composite-key components (<i>keyComponentClass</i>-objects) are
 * automatically retrieved and ordered. The end result is (in effect) a mapping
 * of the composite-key to the value, or multiple mappings if multiple
 * composite-keys are derived in the process (as in the example above when
 * a {@code Book} contains multiple {@code Author} attribute objects).
 * Internal retrieval of the <i>keyComponentClass</i>-objects is done via
 * reflection-based invocation of the <i>valueClass</i>-methods identified during
 * <a href="#construct_KeyComponentProfile">KeyComponentProfile construction</a>.
 * All retrieved <i>keyComponentClass</i>-objects are internally ordered via backing
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html">
 * TreeMap</a>s, either in
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
 * natural order</a>,
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--">
 * hashCode order</a>, or the order maintained by a specified
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html">
 * Comparator</a>. If a <i>keyComponentClass</i> implements the
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
 * Comparable</a> interface, its objects will be ordered in natural order;
 * otherwise they will be ordered in hashCode order. However, if a
 * <a>KeyComponentProfile</a> is
 * {@link KeyComponentProfile#KeyComponentProfile(java.lang.Class, java.lang.Class, java.util.Comparator, java.lang.reflect.Method...)
 * constructed with specification of a Comparator}, its related
 * <i>keyComponentClass</i>-objects will be ordered by the Comparator.
 * <br><br>
 * <b>CONSTRUCTION OF A <i>KeyComponentProfile</i> WITH SPECIFIED GET METHOD(s)</b>
 * <br>
 * In some cases a given <i>keyComponentClass</i> may be returned by multiple
 * <i>valueClass</i>-methods, but it is desired that only a subset of these
 * methods should be the focus of a {@link KeyComponentProfile}. An optional
 * vararg array of Method(s) may be submitted in the <i>KeyComponentProfile</i>
 * {@link KeyComponentProfile#KeyComponentProfile(java.lang.Class, java.lang.Class, java.lang.reflect.Method...)
 * constructor} to specify such a subset.
 * Continuing
 * with the example above, if two methods of the {@code Book} class both return
 * objects of the {@code Date} class ({@code #getPublicationDate} and
 * {@code #getRevisionPublicationDate}), then an <i>OrderedSet</i> for ordering
 * Books via a {@code PublicationDate|Title|Book} composite-key could be
 * constructed as follows (limiting the focus of the first
 * <i>KeyComponentProfile</i> to the {@code #getPublicationDate} method):
 * <PRE>{@code
  OrderedSet<Book> booksOrderedByPublicationDateAndTitle =
      new OrderedSet<Book>(
          new KeyComponentProfile<Book>(Book.class, Date.class,
                          Book.class.getDeclaredMethod("getPublicationDate")),
          new KeyComponentProfile<Book>(Book.class, Title.class)); }</PRE>
 *
 * <a name="keyComponentSetDescription"></a>
 * <b>KeyComponentSets</b>
 * <br>
 * In addition to its ordered list of <i>valueClass</i>-elements, an <i>OrderedSet</i>
 * automatically maintains a <i>keyComponentSet</i> corresponding to each
 * {@link KeyComponentProfile} submitted via the <i>OrderedSet</i> constructor.
 * A <i>keyComponentSet</i> is a unique set of all retrieved
 * <i>keyComponentClass</i>-objects relating to a given <i>KeyComponentProfile</i>.
 * Continuing with the example above, an <i>OrderedSet</i> of {@code Book} values
 * constructed with a {@code Genre|Author|Title} composite-key would maintain
 * three separate <i>keyComponentSet</i>s: a complete unique set of all
 * {@code Genre} objects found in the composite-key entries, a unique set of
 * {@code Author} objects, and a unique set of {@code Title} objects.
 * A <i>keyComponentSet</i> may be retrieved through invocation of the
 * {@link #keyComponentSet(org.commonvox.collections.KeyComponentProfile)
 * keyComponentSet(KeyComponentProfile)} method.
 * A <i>keyComponentSet</i> is a
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeSet.html">
 * TreeSet</a>, ordered either in
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
 * natural order</a>,
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--">
 * hashCode order</a>, or the order maintained by a specified
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html">
 * Comparator</a>.
 * If the related <i>keyComponentClass</i> implements the
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
 * Comparable</a> interface, <i>keyComponentClass</i>-objects will be ordered
 * in natural order; otherwise they will be ordered in hashCode order.
 * However, if the corresponding <a>KeyComponentProfile</a> is
 * {@link KeyComponentProfile#KeyComponentProfile(java.lang.Class, java.lang.Class, java.util.Comparator, java.lang.reflect.Method...)
 * constructed with specification of a Comparator}, the
 * <i>keyComponentClass</i>-objects will be ordered by the Comparator.
 * <br><br>
 * <b>Note that this implementation (which extends the HashSet class) is not
 * synchronized.</b> For more information/advice on
 * potential concurrent usage of this class, please see
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/HashSet.html">
 * the HashSet documentation</a>.
 * <br><br>
 * <hr>
 * <b>Special note on making modifications to a mutable object that is an
 * element in an <i>OrderedSet</i>:</b>
 * Modifications made directly to an object that is an element in an
 * <i>OrderedSet</i> can result in a disordering of the <i>OrderedSet</i>'s
 * internal mapping structures.
 * Therefore, the following sequence must be followed when altering the fields
 * of any mutable object that is an element in an <i>OrderedSet</i>:
 * <ol>
 * <li>{@link #remove(java.lang.Object) Remove} the object from the
 * <i>OrderedSet</i>.</li>
 * <li>Make a
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#clone--">
 * deep-copy clone</a> of the removed object.</li>
 * <li>Make desired modifications to the object-clone.</li>
 * <li>{@link #add(java.lang.Object) Add} the object-clone to the
 * <i>OrderedSet</i>.</li>
 * </ol>
 * <hr>
 * <a name="usage_examples"></a>
 * &nbsp;<br>
 * <b>USAGE EXAMPLES</b>
 * <br>
 * The following examples show <i>composite-key</i> ordering of a set of objects
 * of the {@code Book.class},
 * which has attributes of {@code Author}, {@code Title}, {@code Genre},
 * and {@code Date} classes returned by the methods {@code Book#getAuthors()},
 * {@code Book#getTitle()}, {@code Book#getGenres()},
 * {@code Book#getPublicationDate()} and {@code Book#getRevisionPublicationDate()}.
 * <br><br>
 * Example 1 orders {@code Book}s by {@code Title};
 * Example 2 orders {@code Book}s by {@code Author|Title};
 * Example 3 orders {@code Book}s by {@code PublicationDate|Title} (most
 * recently published listed first).
 * <PRE>{@code
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
  }}</PRE>
 *
 * @author Daniel Vimont
 * @param <V> The <i>valueClass</i>, i.e. the class of objects contained
 * and ordered by this <i>OrderedSet</i>.
 */
public class OrderedSet<V> extends HashSet<V> {

  private MapNode<V> topMapNode;
  private Map<KeyComponentProfile<V>,Set<Object>> keyComponentSetMap;
  private transient Map<Method,List<Set<Object>>> keyComponentSetMapByMethod;
  private boolean removeAllIsInProgress = false;

  /**
   * Constructs an empty <i>OrderedSet</i> configured for
   * composite-key based ordering of its values as specified in the
   * submitted vararg array of {@link KeyComponentProfile} parameters.
   *
   * @param keyComponentProfiles An array of {@link KeyComponentProfile}
   * objects which specifies the composite-key structure which orders the
   * values in the <i>OrderedSet</i>.
   */
  @SafeVarargs
  public OrderedSet(
          KeyComponentProfile<V>... keyComponentProfiles) {
    this(new HashSet<V>(), keyComponentProfiles);
  }

  /**
   * Constructs an <i>OrderedSet</i> containing the values in the
   * specified collection, with the values ordered according to the
   * composite-key specified in the submitted vararg array of
   * {@link KeyComponentProfile} parameters.
   *
   * @param collection Collection of <i>valueClass</i>-objects to be add to
   * and ordered by the new <i>OrderedSet</i>
   * @param keyComponentProfiles An array of {@link KeyComponentProfile}
   * objects which specifies the composite-key structure which orders the
   * values in the <i>OrderedSet</i>.
   */
  @SafeVarargs
  public OrderedSet(Collection<? extends V> collection,
          KeyComponentProfile<V>... keyComponentProfiles) {
    MapNode.checkVarargs(keyComponentProfiles);
    this.topMapNode = new MapNode<V>("topMapNode", keyComponentProfiles);
    buildKeyComponentSets();
    addAll(collection);
  }


  private void buildKeyComponentSets() {
    keyComponentSetMap = new HashMap<KeyComponentProfile<V>, Set<Object>>();
    for (KeyComponentProfile keyComponentProfile :
            topMapNode.getKeyComponentProfileList()) {
      if (keyComponentProfile.getKeyComponentBasis().equals(
              KeyComponentProfile.KeyComponentBasis.IDENTITY)) {
        continue;
      }
      Comparator comparator = keyComponentProfile.getKeyComponentClassComparator();
      if (comparator != null) {
        keyComponentSetMap.put(keyComponentProfile, new TreeSet<Object>(comparator));
      } else if (Comparable.class.isAssignableFrom(
              keyComponentProfile.getKeyComponentClass())) {
        keyComponentSetMap.put(keyComponentProfile, new TreeSet<Object>());
      } else {
        keyComponentSetMap.put(
                keyComponentProfile, new TreeSet<Object>(new HashCodeComparator()));
      }
    }
    buildTransientCollections();
  }

  /**
   * Note that the Method class is not Serializable; thus, the collections
   * that contain Method objects are not Serializable and must be rebuilt
   * as part of the deserialization process.
   * This method builds keyComponentSetMapByMethod: a HashMap that maps each
   * "get" method of the valueClass with a List of all Sets of key-component
   * objects that the "get" method can be used to populate via reflective
   * invocation.
   */
  private void buildTransientCollections() {
    keyComponentSetMapByMethod = new HashMap<Method,List<Set<Object>>>();

    for (Entry<KeyComponentProfile<V>, Set<Object>> entry :
            keyComponentSetMap.entrySet()) {
      for (Method methodToGetKeyComponent :
             entry.getKey().getKeyComponentGetMethods()) {
        List<Set<Object>> keyComponentSetList;
        if (keyComponentSetMapByMethod.containsKey(methodToGetKeyComponent)) {
          keyComponentSetList
                  = keyComponentSetMapByMethod.get(methodToGetKeyComponent);
        } else {
          keyComponentSetList = new ArrayList<Set<Object>>();
        }
        keyComponentSetList.add(entry.getValue());
        keyComponentSetMapByMethod.put(methodToGetKeyComponent, keyComponentSetList);
      }
    }
  }

  /**
   * Adds the specified value (i.e., object) to this
   * <i>OrderedSet</i> if it is not already present, and orders the
   * value via the composite-key stipulated by the
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * keyComponentProfiles} of the set. A null value is not accepted.
   *
   * @param value Value (i.e., object) to be added to and ordered by this set.
   * @return {@code true} if this set did not already contain the specified
   * element. The value may not be null.
   */
  @Override
  public final boolean add(V value) {
    return addOrRemove(value, false);
  }

  private boolean addOrRemove(V value, boolean removeValue) {
    if (value == null) {
      throw new IllegalArgumentException("Invalid <null> value "
              + "submitted to add/remove method.");
    }
    if (!removeValue) {
      if (!super.add(value)) {
        return false;
      }
    }
    topMapNode.autoMap(value, removeValue);
    if (!removeValue) {
      autoPopulateKeyComponentSets(value);
    }
    return true;
  }

  /**
   * Removes the specified object from this set if it is present, and clears
   * any corresponding composite-key entries.
   * Note that invocation of this method will result in a rebuilding of
   * internal <a href="#keyComponentSetDescription"><i>keyComponentSet</i>s</a>.
   *
   * @param o Object to be removed from this set, if present.
   * @return {@code true} if the set contained the specified object.
   */
  @Override
  public boolean remove(Object o) {
    boolean removed;
    if (removed = super.remove(o)) {
      removeFromMapNodes((V)o);
      if (!removeAllIsInProgress) {
        repopulateKeyComponentSets();
      }
    }
    return removed;
  }

  /**
   * Removes each object in the specified collection from this set if it is
   * present, and clears any corresponding composite-key entries.
   * Note that invocation of this method will result in a
   * rebuilding of internal
   * <a href="#keyComponentSetDescription"><i>keyComponentSet</i>s</a>.
   *
   * @param collection Objects to be removed from this set, if present.
   * @return {@code true} if this set changed as a result of the call.
   */
  @Override
  public boolean removeAll(Collection<?> collection) {
    removeAllIsInProgress = true;
    boolean thisCollectionChanged = false;
    for (Object o : collection) {
      boolean objectRemoved;
      if (objectRemoved = remove(o)) {
        removeFromMapNodes((V)o);
        thisCollectionChanged = true;
      }
    }
    if (thisCollectionChanged) {
      repopulateKeyComponentSets();
    }
    removeAllIsInProgress = false;
    return thisCollectionChanged;
  }

  private boolean removeFromMapNodes(V object) {
    return addOrRemove(object, true);
  }

  /**
   * Removes all of the values from this set.
   * The set will be empty after this call returns.
   */
  @Override
  public void clear() {
    // Construct OrderedSet copy with empty MapNode structures.
    OrderedSet<V> emptyCopyOfOrderedSet
            = new OrderedSet<V>(
                    topMapNode.getKeyComponentProfileList().toArray(new KeyComponentProfile[
                                    topMapNode.getKeyComponentProfileList().size()]));

    super.clear();
    this.topMapNode = emptyCopyOfOrderedSet.topMapNode;
    this.keyComponentSetMap
            = emptyCopyOfOrderedSet.keyComponentSetMap;
    this.keyComponentSetMapByMethod
            = emptyCopyOfOrderedSet.keyComponentSetMapByMethod;
  }

  /**
   * Invokes this collection's inherited
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/HashSet.html#iterator--">
   * HashSet iterator functionality</a>;
   * when the
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html#remove--"
   * target="_blank">Iterator#remove</a> method is invoked,
   * the specified object is removed, along with any corresponding
   * composite-key entries.
   *
   * @return Returns an iterator over the values in the collection.
   */
  @Override
  public Iterator<V> iterator() {
    final Iterator<V> superIterator = super.iterator();
    return new Iterator<V>() {
      V currentObject;

      @Override
      public boolean hasNext() {
        return superIterator.hasNext();
      }

      @Override
      public V next() {
        return currentObject = superIterator.next();
      }

      @Override
      public void remove() {
        superIterator.remove();
        if (currentObject != null) {
          removeFromMapNodes(currentObject);
          repopulateKeyComponentSets();
        }
      }
    };
  }

  private void autoPopulateKeyComponentSets(V value) {
    if (keyComponentSetMapByMethod == null) {
      return;
    }
    for (Map.Entry<Method,List<Set<Object>>> entry :
            keyComponentSetMapByMethod.entrySet()) {
      Method currentGetMethod = entry.getKey();
      List<Set<Object>> keyComponentSetsPopulatedByCurrentGetMethod = entry.getValue();

      if (keyComponentSetsPopulatedByCurrentGetMethod == null ||
              keyComponentSetsPopulatedByCurrentGetMethod.isEmpty()) {
        continue;
      }
      if (Collection.class.isAssignableFrom(currentGetMethod.getReturnType())) {
        Collection<Object> keyComponents = null;
        try {
          @SuppressWarnings("unchecked")
          Collection<Object> retrievedKeyComponents
                  = (Collection<Object>) currentGetMethod.invoke(value);
          keyComponents = retrievedKeyComponents;
        }
        catch (InvocationTargetException e) {
          throw new InternalError("Unanticipated " + e.getClass().getSimpleName()
                  + " encountered while retrieving keyComponent(s).", e);
        }
        catch (IllegalAccessException e) {
          throw new InternalError("Unanticipated " + e.getClass().getSimpleName()
                  + " encountered while retrieving Component(s).", e);
        }
        if (keyComponents == null) {
          continue;
        }
        for (Set<Object> keyComponentSet :
                keyComponentSetsPopulatedByCurrentGetMethod) {
          keyComponentSet.addAll(keyComponents);
        }
      } else { // get a single instance of keyComponent
        Object keyComponent = null;
        try {
          keyComponent = currentGetMethod.invoke(value);
        }
        catch (InvocationTargetException e) {
          throw new InternalError("Unanticipated " + e.getClass().getSimpleName()
                  + " encountered while retrieving keyComponent.", e);
        }
        catch (IllegalAccessException e) {
          throw new InternalError("Unanticipated " + e.getClass().getSimpleName()
                  + " encountered while retrieving keyComponent.", e);
        }
        if (keyComponent == null) {
          continue;
        }
        for (Set<Object> keyComponentSet :
                keyComponentSetsPopulatedByCurrentGetMethod) {
          keyComponentSet.add(keyComponent);
        }
      }
    }
  }

  private void repopulateKeyComponentSets() {
    buildKeyComponentSets();
    for (V object : this) {
      autoPopulateKeyComponentSets(object);
    }
  }

  /**
   * Returns a Set of the composite-keys in this <i>OrderedSet</i>.
   * Each composite-key is in the format of a List of the
   * <i>keyComponentClass</i>-objects of which it is comprised.
   * This Set of composite-keys matches the key portion of the entries returned
   * by the {@link #entrySet()} method.
   *
   * @return Set consisting of <i>keyComponentClass</i>-object Lists.
   */
  public final Set<List<Object>> compositeKeys() {
    return topMapNode.getKeyComponentLists();
  }

  /**
   * The composite-key-ordered entries of this <i>OrderedSet</i>, with
   * composite-key instances structured and ordered according to the array of
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * KeyComponentProfiles} with which the <i>OrderedSet</i> was constructed.
   * Each entry pairs a composite-key instance (comprised of a List of
   * <i>keyComponentClass</i>-objects) with the value to which it is mapped.
   * The returned set is not backed by the <i>OrderedSet</i>, so a change
   * to one (e.g., add, remove, etc.) is not reflected in the other.
   *
   * @return Entry set in composite-key order, with each entry pairing a
   * composite-key instance (comprised of a List of
   * <i>keyComponentClass</i>-objects) with the value to which it is mapped.
   */
  public final Set<Map.Entry<List<Object>,V>> entrySet() {
    return topMapNode.getEntrySet();
  }

  /**
   * Returns a List of the values contained in this <i>OrderedSet</i>, ordered
   * according to composite-key structures stipulated by the array of
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * KeyComponentProfiles} with which the <i>OrderedSet</i> was constructed.
   *
   * @return values in composite-key order
   */
  public final List<V> values() {
    return topMapNode.selectAll();
  }

  /**
   * Returns a List of the values contained in this <i>OrderedSet</i>, ordered
   * according to composite-key structures stipulated by the array of
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * KeyComponentProfiles} with which the <i>OrderedSet</i> was constructed; if
   * the suppressConsecutiveDuplicates parameter is {@code true}, then any
   * consecutive duplicate objects in the List will be suppressed.
   *
   * @param suppressConsecutiveDuplicates if {@code true}, consecutive
   * duplicates are removed from the List before it is returned.
   * @return values in composite-key order
   */
  public final List<V> values (boolean suppressConsecutiveDuplicates) {
    if (suppressConsecutiveDuplicates) {
      return suppressConsecutiveDuplicates(values());
    } else {
      return values();
    }
  }

  /**
   * Returns a List of the values contained in this <i>OrderedSet</i> which
   * share the submitted <i>keyComponent</i>-object as a common attribute;
   * the submitted <i>keyComponent</i>-object must be of class equal to the
   * <i>keyComponentClass</i> stipulated in the first
   * {@link KeyComponentProfile} submitted to the constructor of this
   * <i>OrderedSet</i>.
   * The List is ordered according to composite-key structures stipulated by
   * the array of
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * KeyComponentProfiles} with which the <i>OrderedSet</i> was constructed.
   * <br><br>
   * For an example of effective usage of this method in conjunction
   * with the {@link #keyComponentSet(org.commonvox.collections.KeyComponentProfile)
   * #keyComponentSet(KeyComponentProfile)} method, please see {@code EXAMPLE 2}
   * in the <a href="#usage_examples">USAGE EXAMPLES</a>.
   *
   * @param keyComponentObject Object of class equal to the
   * <i>keyComponentClass</i> stipulated in the first
   * {@link KeyComponentProfile} submitted to the constructor of this
   * <i>OrderedSet</i>.
   * @return A List of the values contained in this <i>OrderedSet</i> which
   * share the submitted <i>keyComponent</i>-object as a common attribute.
   * @throws IllegalArgumentException if keyComponentObject is not of class
   * equal to the <i>keyComponentClass</i> stipulated in the first
   * {@link KeyComponentProfile} submitted to the constructor of this
   * <i>OrderedSet</i>.
   */
  public final List<V> values(Object keyComponentObject)
      throws IllegalArgumentException {
    if (!topMapNode.getKeyComponentProfileList().get(0).getKeyComponentClass().
            isAssignableFrom(keyComponentObject.getClass())) {
      throw new IllegalArgumentException("First KeyComponentProfile "
              + "of this OrderedSet is based on Class : <"
              + topMapNode.getKeyComponentProfileList().get(0).getKeyComponentClass()
              + ">. It is NOT based on Class of submitted keyComponentObject: <"
              + keyComponentObject.getClass().getName() + ">.");
    }
    return new ArrayList<V>(new LinkedHashSet<V>(
            this.topMapNode.get(keyComponentObject)));
  }

  /**
   * Returns the list of {@link KeyComponentProfile}s with which this
   * <i>OrderedSet</i> was
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * constructed}.
   *
   * @return The list of {@link KeyComponentProfile}s with which this
   * <i>OrderedSet</i> was
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * constructed}.
   */
  public List<KeyComponentProfile<V>> getKeyComponentProfiles() {
    return this.topMapNode.getKeyComponentProfileList();
  }

  private List<V> suppressConsecutiveDuplicates(List<V> allValues) {
    List<V> returnedValues = new ArrayList<V>();
    V previousValue = null;
    for (V value : allValues) {
      if (!value.equals(previousValue)) {
        returnedValues.add(value);
        previousValue = value;
      }
    }
    return returnedValues;
  }

  /**
   * Returns a Set view of all <i>keyComponentClass</i>-objects related to the
   * submitted {@link KeyComponentProfile}. The <i>KeyComponentProfile</i>
   * must be one (or {@link KeyComponentProfile#equals(java.lang.Object) equal}
   * to one) that was submitted in the
   * {@link #OrderedSet(org.commonvox.collections.KeyComponentProfile...)
   * constructor} of this <i>OrderedSet</i>.
   * <br><br>
   * For example, an <i>OrderedSet</i> of {@code Book}-class objects
   * constructed with a {@code Genre|Author} composite-key would maintain
   * two separate <i>keyComponentSet</i>s: a complete unique set of all
   * {@code Genre} objects found in the composite-key entries, and a unique set
   * of {@code Author} objects, as in the following example code:
   * <PRE>{@code  KeyComponentProfile<Book> genreComponent
          = new KeyComponentProfile<Book>(Book.class, Genre.class);
  KeyComponentProfile<Book> authorComponent
          = new KeyComponentProfile<Book>(Book.class, Author.class);
  OrderedSet<Book> booksByGenreAndAuthor
          = new OrderedSet<Book>(myBookList, genreComponent, authorComponent);

  Set<Object> genreSet = booksByGenreAndAuthor.keyComponentSet(genreComponent);
  Set<Object> authorSet = booksByGenreAndAuthor.keyComponentSet(authorComponent); }</PRE>
   * A <i>keyComponentSet</i> is a
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeSet.html">
   * TreeSet</a>, ordered either in
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
   * natural order</a>,
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--">
   * hashCode order</a>, or the order maintained by a specified
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html">
   * Comparator</a>.
   * If the related <i>keyComponentClass</i> implements the
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
   * Comparable</a> interface, <i>keyComponentClass</i>-objects will be ordered
   * in natural order; otherwise they will be ordered in hashCode order.
   * However, if the corresponding <a>KeyComponentProfile</a> is
   * {@link KeyComponentProfile#KeyComponentProfile(java.lang.Class, java.lang.Class, java.util.Comparator, java.lang.reflect.Method...)
   * constructed with specification of a Comparator}, the
   * <i>keyComponentClass</i>-objects will be ordered by the Comparator.
   *
   * @param keyComponentProfile One of the {@link KeyComponentProfile} objects
   * that was submitted in the constructor of this <i>OrderedSet</i>.
   * @return Set of <i>keyComponentClass</i>-objects related to the submitted
   * <i>KeyComponentProfile</i>.
   * @throws IllegalArgumentException if submitted keyComponentProfile not
   * found in this <i>OrderedSet</i>.
   */
  public Set<Object> keyComponentSet (KeyComponentProfile<V> keyComponentProfile)
      throws IllegalArgumentException {
    if (!keyComponentSetMap.containsKey(keyComponentProfile)) {
      throw new IllegalArgumentException("Submitted KeyComponentProfile "
              + "not found in this OrderedSet.");
    }
    return keyComponentSetMap.get(keyComponentProfile);
  }

  /**
   * Special "override" of readObject required to enable deserialization; all
   * index structures that contain Method objects (not serializable) must be
   * reconstructed. For more info, see:
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html"
   * target="_blank">
   * Serializable documentation</a>
   *
   * @param in ObjectInputStream object.
   * @throws java.io.IOException from defaultReadObject
   * @throws java.lang.ClassNotFoundException from defaultReadObject
   */
  private void readObject(ObjectInputStream in)
          throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    // rebuild structures that contain Method objects (not serializable)
    if (keyComponentSetMap != null) {
      buildTransientCollections();
    }
  }

  /**
   * Returns a String showing this object's <i>keyComponentClass</i> structure.
   *
   * @return A String showing this object's <i>keyComponentClass</i> structure.
   */
  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();
    output.append("\nSTRUCTURE of ").
            append(OrderedSet.class.getSimpleName()).
            append("\n*****").
            append("\n  ").
            append(topMapNode.getInternalsReport()).
            append("\n*****\n");
    return output.toString();
  }

  /**
   * Outputs full internal listing of <i>OrderedSet</i> contents,
   * mainly for debugging purposes.
   */
  void dumpContents() {
    topMapNode.dumpContents();
  }

  private class HashCodeComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      return o1.hashCode() - o2.hashCode();
    }
  }
}
