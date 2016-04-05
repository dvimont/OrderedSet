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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A <i>MapNode</i> provides for programmer-stipulated ordering of
 * elements that are contained in an {@link OrderedSet}.
 * The order of an <i>MapNode</i> is stipulated in the constructor's
 * {@link KeyComponentProfile} vararg array parameters, which are passed on
 * from the <i>OrderedSet</i>'s
 * {@link OrderedSet#OrderedSet(org.commonvox.collections.KeyComponentProfile...)
 * constructor}.
 *
 * @author Daniel Vimont
 * @param <V> The <i>valueClass</i>, i.e. the class of elements contained in
 * the {@link OrderedSet} to which the <i>MapNode</i> belongs.
 * The <i>valueClass</i> must match the {@literal <V>} type-parameter
 * of any {@link OrderedSet} to which the <i>MapNode</i> belongs.
 */
class MapNode<V> implements Serializable, Cloneable {

  private static long nodeCount = 0;
  private final String mapNodeTitle;
  private final boolean topLevelNode;
  private Map<Object, MapNode<V>> middleNode = null;
  private Map<Object, V> bottomNode = null;
  private final LinkedList<KeyComponentProfile<V>> keyComponentProfileList;
  private final int immutableHashCode;

  /**
   * Constructs a <i>MapNode</i> for ordering of objects belonging to the
   * class specified by <i>valueClass</i>, in the order specified by one or more
   * {@link KeyComponentProfile}s.
   * @param title Optional title of the <i>MapNode</i>.
   * @param keyComponentProfiles Array of {@link KeyComponentProfile}
   * objects establishing the ordering of the {@link OrderedSet}
   * to which the <i>MapNode</i> belongs.
   */
  @SafeVarargs
  public MapNode(String title, KeyComponentProfile<V>... keyComponentProfiles) {
    MapNode.checkVarargs(keyComponentProfiles);
    this.mapNodeTitle = title;
    this.topLevelNode = true;
    this.keyComponentProfileList
            = new LinkedList<KeyComponentProfile<V>>(
                    Arrays.asList(keyComponentProfiles));
    // To assure composite-key completeness, final KeyComponentProfile must
    // be IDENTITY - i.e., based on the valueClass itself.
    if (!keyComponentProfiles[keyComponentProfiles.length - 1].getKeyComponentBasis().
            equals(KeyComponentProfile.KeyComponentBasis.IDENTITY)) {
      keyComponentProfileList.add(new KeyComponentProfile<V>());
    }
    immutableHashCode = computeImmutableHashCode();
    nodeCount++;
  }

  /**
   * This private constructor only usable by a MapNode instance to create
   * other (lower level) MapNode nodes (instances).
   *
   * @param title Title of the <i>MapNode</i>.
   * @param keyComponentProfiles Array of {@link KeyComponentProfile}
   * objects establishing the ordering of the <i>MapNode</i>.
   * @param multiKeyEntries one or more {@link MultiKeyComponentEntry} objects to be
   * submitted to #putOrRemove in the newly created MapNode.
   */
  @SafeVarargs
  private MapNode(String title,
          LinkedList<KeyComponentProfile<V>> keyComponentProfiles,
          MultiKeyComponentEntry<V>... multiKeyEntries) {

    MapNode.checkVarargs(multiKeyEntries);
    this.topLevelNode = false;
    this.keyComponentProfileList = keyComponentProfiles;
    this.mapNodeTitle = null;
    this.immutableHashCode = computeImmutableHashCode();
    nodeCount++;
    for (MultiKeyComponentEntry<V> multiKeyComponentEntry : multiKeyEntries) {
      putOrRemove(multiKeyComponentEntry, false);
    }
  }

  /**
   * Get total number of active nodes in all MapNodes for audit purposes
   *
   * @return Total node count
   */
  static long getNodeCount() {
    return nodeCount;
  }

  /**
   * This method may be invoked by any method which accepts vararg parameters,
   * but for which it is intended that (1) a null or empty vararg
   * array is invalid, and (2) that no entry in the array may be null.
   *
   * @param objectArray array of objects
   * @throws IllegalArgumentException if objectArray is null
   */
  static void checkVarargs(Object[] objectArray)
          throws IllegalArgumentException {
    if (objectArray == null || objectArray.length == 0) {
      throw new IllegalArgumentException("Invalid null or zero-length array "
              + "submitted for varargs parameters.");
    }
    for (Object object : objectArray) {
      if (object == null) {
        throw new IllegalArgumentException("Invalid null value submitted "
                + "as part of varargs parameters array.");
      }
    }
  }

  /**
   * Add an entry to this MapNode. (Analogous to put method of a Map,
   * except that in this case there are multiple [composite] keys.)
   *
   * @param multiKeyComponentEntry containing value and an array of
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
   * Comparable</a>s
   * @return <code>true</code> if putOrRemove succeeds <code>false</code> if
   * putOrRemove fails
   */
  private boolean putOrRemove(MultiKeyComponentEntry<V> multiKeyComponentEntry,
          boolean removeValue) {
    if (multiKeyComponentEntry.containsNulls()
            || multiKeyComponentEntry.getKeyComponentArrayLength() == 0) {
      throw new InternalError("Invalid MultiKeyComponentEntry "
              + "submitted for 'put' into MapNode <" + getTitle()
              + " LEVEL " + this.keyComponentProfileList.size() + ">. "
              + "MultiKeyComponentEntry "
              + (multiKeyComponentEntry.containsNulls() ? "contains null(s)." : "")
              + (multiKeyComponentEntry.getKeyComponentArrayLength() == 0 ?
                      "has zero length key array." : ""));
    }
    KeyComponentProfile topKeyComponentProfile
            = keyComponentProfileList.get(0);
    if (!(topKeyComponentProfile.getKeyComponentBasis().
                equals(KeyComponentProfile.KeyComponentBasis.IDENTITY)
            || (topKeyComponentProfile.getKeyComponentClass().isAssignableFrom(multiKeyComponentEntry.getTopKeyComponent().getClass())))) {
      throw new InternalError("Invalid keyComponent object "
              + "submitted for 'put' into MapNode <" + getTitle()
              + " LEVEL " + this.keyComponentProfileList.size()
              + ">. Requires a keyComponent object of class (or subclass of) <"
              + topKeyComponentProfile.getKeyComponentClass().getSimpleName()
              + ">. Class of the invalid submitted object is <"
              + multiKeyComponentEntry.getTopKeyComponent().getClass().getSimpleName() + ">.");
    }
    if (multiKeyComponentEntry.getKeyComponentArrayLength() == 1) {
      if (this.bottomNode == null) {
        if (topKeyComponentProfile.getKeyComponentClassComparator() != null) {
          bottomNode = new TreeMap<Object, V>(
                          topKeyComponentProfile.getKeyComponentClassComparator());
        } else if (Comparable.class.isAssignableFrom(
                      multiKeyComponentEntry.getValue().getClass())) {
          bottomNode = new TreeMap<Object, V>();
        } else {
          bottomNode = new TreeMap<Object, V>(new HashCodeComparator());
        }
      }
      V selectValue = this.bottomNode.get(multiKeyComponentEntry.getTopKeyComponent());
      if (selectValue == null && !removeValue) {
        this.bottomNode.put(multiKeyComponentEntry.getTopKeyComponent(), multiKeyComponentEntry.getValue());
        return true;
      } else {
        if (removeValue) {
          return this.bottomNode.remove(multiKeyComponentEntry.getTopKeyComponent(),
                  multiKeyComponentEntry.getValue());
        } else {
          return false; // no overwriting of bottomNode values accepted
        }
      }
    } else {
      if (this.middleNode == null) {
        if (topKeyComponentProfile.getKeyComponentClassComparator() != null) {
          middleNode = new TreeMap<Object, MapNode<V>>(
                          topKeyComponentProfile.getKeyComponentClassComparator());
        } else if (Comparable.class.isAssignableFrom(multiKeyComponentEntry.getTopKeyComponent().getClass())) {
          middleNode = new TreeMap<Object, MapNode<V>>();
        } else {
          middleNode = new TreeMap<Object, MapNode<V>>(new HashCodeComparator());
        }
      }
      MapNode<V> lowerMapNode
              = this.middleNode.get(multiKeyComponentEntry.getTopKeyComponent());
      MultiKeyComponentEntry<V> lowerMultiKeyComponentEntry
              = multiKeyComponentEntry.getLowerEntry();
      if (lowerMapNode == null) {
        this.middleNode.put(multiKeyComponentEntry.getTopKeyComponent(),
                new MapNode<V>(
                        this.mapNodeTitle,
                        this.getLowerKeyComponentProfileList(),
                        lowerMultiKeyComponentEntry));
        return true;
      } else {
        return lowerMapNode.putOrRemove(lowerMultiKeyComponentEntry, removeValue);
      }
    }
  }

  private LinkedList<KeyComponentProfile<V>> getLowerKeyComponentProfileList() {
    if (keyComponentProfileList.isEmpty()) {
      return keyComponentProfileList;
    }
    return new LinkedList<KeyComponentProfile<V>>(
            keyComponentProfileList.subList(1, keyComponentProfileList.size()));
  }

  /**
   * Get the "depth" of this MapNode as denoted by the size of its
   * KeyComponentProfile list.
   *
   * @return The "depth" of this MapNode as denoted by the size of its
   * KeyComponentProfile list.
   */
  int getDepth() {
      return this.keyComponentProfileList.size();
  }

  /**
   * Get MapNode title for audit purposes
   *
   * @return mapNodeTitle
   */
  String getTitle() {
    return this.mapNodeTitle;
  }

  List<KeyComponentProfile<V>> getKeyComponentProfileList() {
    return this.keyComponentProfileList;
  }

  /**
   * Invoked to get all values contained in this MapNode, in ordered
   * sequence.
   *
   * @return All values contained in this MapNode, in ordered sequence.
   */
  final List<V> selectAll() {
    List<V> vList = new ArrayList<V>();
    if (bottomNode != null && !bottomNode.isEmpty()) {
      vList.addAll(this.bottomNode.values());
    }
    if (middleNode != null && !middleNode.isEmpty()) {
      for (MapNode<V> lowerMapNode : this.middleNode.values()) {
        vList.addAll(lowerMapNode.selectAll());
      }
    }
    return vList;
  }

  /**
   * Invoked to get all values with composite-key matching the submitted
   * keyComponentArray.
   *
   * @param keyComponentArray array of key objects.
   * @return all values with composite-key matching the submitted keyComponentArray
   */
  @SafeVarargs
  final List<V> get(Object... keyComponentArray) {
    return get(new KeyComponentArray(keyComponentArray));
  }

  /**
   * Invoked to get all values with composite-key matching the submitted
   * keyComponentArray.
   *
   * @param keyComponentArray full or partial KeyComponentArray
   * @return all values with composite-key matching the submitted keyComponentArray
   */
  private List<V> get(KeyComponentArray keyComponentArray) {
    List<V> vList = new ArrayList<V>();

    if (bottomNode != null && !bottomNode.isEmpty()) {
      if (keyComponentArray.getTopKeyComponent() == null) {
        vList.addAll(selectAll());
      } else {
        V bottomNodeValue = this.bottomNode.get(keyComponentArray.getTopKeyComponent());
        if (bottomNodeValue != null) {
          vList.add(bottomNodeValue);
        }
      }
    }
    if (middleNode != null && !middleNode.isEmpty()) {
      if (keyComponentArray.getTopKeyComponent() == null) {
        for (MapNode<V> lowerMapNode : this.middleNode.values()) {
          vList.addAll(lowerMapNode.get(keyComponentArray.getLowerKeyComponentArray()));
        }
      } else {
        if (this.middleNode.containsKey(keyComponentArray.getTopKeyComponent())) {
          MapNode<V> lowerMapNode
                  = this.middleNode.get(keyComponentArray.getTopKeyComponent());
          vList.addAll(lowerMapNode.get(keyComponentArray.getLowerKeyComponentArray()));
        }
      }
    }
    return vList;
  }

  /**
   * Size of this MapNode, as denoted by the number of valueClass-objects
   * that it and its children MapNodes contain.
   *
   * @return Size of this MapNode, as denoted by the number of valueClass-objects
   * that it and its children MapNodes contain.
   */
  int size() {
    int size = 0;
    if (bottomNode != null && !bottomNode.isEmpty()) {
      size += this.bottomNode.size();
    }
    if (middleNode != null && !middleNode.isEmpty()) {
      for (MapNode<V> lowerMapNode : this.middleNode.values()) {
        size += lowerMapNode.size();
      }
    }
    return size;
  }

  private Set<MultiKeyComponentEntry<V>> getMultiKeyComponentEntries() {
    Set<MultiKeyComponentEntry<V>> multiKeyComponentEntries
            = new LinkedHashSet<MultiKeyComponentEntry<V>>();
    if (bottomNode != null && !bottomNode.isEmpty()) {
      for (Entry<Object, V> entry : bottomNode.entrySet()) {
        multiKeyComponentEntries.add(
                new MultiKeyComponentEntry<V>(entry.getValue(), entry.getKey()));
      }
    }
    if (middleNode != null && !middleNode.isEmpty()) {
      for (Entry<Object, MapNode<V>> entry
              : this.middleNode.entrySet()) {
        Set<MultiKeyComponentEntry<V>> lowerMultiKeyComponentEntries
                = entry.getValue().getMultiKeyComponentEntries();
        for (MultiKeyComponentEntry<V> lowerMultiKeyComponentEntry
                : lowerMultiKeyComponentEntries) {
          lowerMultiKeyComponentEntry.putTopKeyComponent(entry.getKey());
          multiKeyComponentEntries.add(lowerMultiKeyComponentEntry);
        }
      }
    }
    return multiKeyComponentEntries;
  }

  final Set<Map.Entry<List<Object>,V>> getEntrySet() {
    Map<List<Object>,V> keyListToValueMap = new LinkedHashMap<List<Object>,V>();
    for (MultiKeyComponentEntry<V> multiKeyComponentEntry :
            getMultiKeyComponentEntries()) {
      keyListToValueMap.put(
              multiKeyComponentEntry.getKeyComponentList(),
              multiKeyComponentEntry.getValue());
    }
    return keyListToValueMap.entrySet();
  }

  final Set<List<Object>> getKeyComponentLists() {
    Set<List<Object>> keyComponentLists = new LinkedHashSet<List<Object>>();
    for (MultiKeyComponentEntry<V> multiKeyComponentEntry :
            getMultiKeyComponentEntries()) {
      keyComponentLists.add(multiKeyComponentEntry.getKeyComponentList());
    }
    return keyComponentLists;
  }

  /**
   * Automap adds or removes the submitted value object to the MapNode structures.
   *
   * @param value valueClass object
   * @return {@code true} if value successfully added
   */
  final boolean autoMap(V value, boolean removeValue) {
    LinkedList<KeyComponentProfile> keyComponentProfileLinkedList
            = new LinkedList<KeyComponentProfile>(keyComponentProfileList);
    return buildAndPutEntry(value, keyComponentProfileLinkedList,
            new LinkedList<Object>(), removeValue);
  }

  /**
   * This method is invoked recursively to build one or multiple sets of (value,
   * keyComponentList) combinations, and ultimately to submit each completed set
   * to the MapNode's #putOrRemove method.
   *
   * @param value The Object (of class {@literal <V>}) which is being ordered.
   * @param keyComponentProfileLinkedList With each recursive invocation, the
   * top getMethod is "popped" off of the top of this list, and the getMethod
   * invoke. A final recursion is identified by the fact that the list is empty;
   * at that point, a (value, keyComponentList) combination is submitted to the
   * #putOrRemove method.
   * @param keyComponentList List of keyComponent-objects assembled in the
   * recursive invocations of this method.
   * @return {@code true} if final puts are all successful
   */
  private boolean buildAndPutEntry(V value,
          LinkedList<KeyComponentProfile> keyComponentProfileLinkedList,
          LinkedList<Object> keyComponentList, boolean removeValue) {

    if (keyComponentProfileLinkedList.isEmpty()) {
      return putOrRemove(new MultiKeyComponentEntry<V>(keyComponentList, value), removeValue);
    }

    boolean allPutsSuccessful = true;

    Set<Object> keyComponentSet
            = keyComponentProfileLinkedList.removeFirst().getKeyComponentSet(value);
    for (Object keyComponent : keyComponentSet) {
      LinkedList<KeyComponentProfile> copyOfKeyComponentProfileLinkedList
              = new LinkedList<KeyComponentProfile>(keyComponentProfileLinkedList);
      LinkedList<Object> copyOfKeyComponentList
              = new LinkedList<Object>(keyComponentList);
      copyOfKeyComponentList.add(keyComponent);
      boolean putsSuccessful
              = buildAndPutEntry(value,
                      copyOfKeyComponentProfileLinkedList,
                      copyOfKeyComponentList, removeValue);
      if (!putsSuccessful) {
        allPutsSuccessful = false;
      }
    }
    return allPutsSuccessful;
  }

  MapNode<V> cloneWithoutValues() {
    return new MapNode<V>(mapNodeTitle,
            keyComponentProfileList.toArray(
                    new KeyComponentProfile[keyComponentProfileList.size()]));
  }

  boolean isTopLevelNode() {
    return topLevelNode;
  }

  private int computeImmutableHashCode() {
    int hash = 5;
    hash = 23 * hash + (this.topLevelNode ? 1 : 0);
    hash = 23 * hash + (this.keyComponentProfileList != null ? this.keyComponentProfileList.hashCode() : 0);
    return hash;
  }

  @Override
  public int hashCode() {
    return immutableHashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MapNode other = (MapNode) obj;
    if (this.topLevelNode != other.topLevelNode) {
      return false;
    }
    if (this.keyComponentProfileList != other.keyComponentProfileList &&
            (this.keyComponentProfileList == null ||
            !this.keyComponentProfileList.equals(other.keyComponentProfileList))) {
      return false;
    }
    return true;
  }

  /**
   * Returns a String containing a brief report of the internal structures of
 this MapNode.
   *
   * @return A String containing a brief report of the internal structures of
 this MapNode.
   */
  String getInternalsReport() {
    return "<" + this.mapNodeTitle + "> MapNode -- \n"
            + "     " + keyComponentClassListToString() + ".\n"
            + "     NUMBER OF MapNode ENTRIES = " + this.size()
            + " ; DEPTH OF MapNode = "
            + keyComponentProfileList.size();
  }

  /**
   * Prints verbose listing of MapNode contents; mainly for test/debug.
   */
  void dumpContents() {
    StringBuilder output;
    boolean firstItemPrinted;
    printHeadingWithTimestamp("Dump of <" + this.getTitle()
            + "> MapNode structures & content", this.size());
    System.out.println(keyComponentClassListToString());
    printHeading("<VALUE toString> : {KEY1; KEY2; KEY3...}");
    for (MultiKeyComponentEntry<V> multiKeyComponentEntry :
            getMultiKeyComponentEntries()) {
      output = new StringBuilder();
      output.append(multiKeyComponentEntry.getValue()).append(" : {");
      firstItemPrinted = false;
      for (Object key : multiKeyComponentEntry.getKeyComponentArray()) {
        if (firstItemPrinted) {
          output.append("; ");
        } else {
          firstItemPrinted = true;
        }
        output.append("<").append(key).append(">");
      }
      output.append("}");
      System.out.println(output);
    }
  }

  private String keyComponentClassListToString() {
    StringBuilder output = new StringBuilder();
    output.append("Key-component classes for this MapNode are: {");
    boolean firstItemPrinted = false;
    for (KeyComponentProfile keyComponentProfile : keyComponentProfileList) {
      if (firstItemPrinted) {
        output.append("; ");
      } else {
        firstItemPrinted = true;
      }
      output.append("<").append(keyComponentProfile.getKeyComponentClass().
              getSimpleName()).append(">");
    }
    output.append("}");
    return output.toString();
  }

  private static void printHeading(String headingTitle) {
    String headingBorder
            = new String(new char[headingTitle.length()]).replace("\0", "=");

    System.out.println(headingBorder);
    System.out.println(headingTitle);
    System.out.println(headingBorder);
  }

  private static void printHeadingWithTimestamp(String headingTitle, int mapNodeSize) {
    String headingBorder
            = new String(new char[headingTitle.length()]).replace("\0", "=");

    System.out.println(headingBorder);
    System.out.println(headingTitle + "  ("
            + new Timestamp(System.currentTimeMillis()) + ")");
    if (mapNodeSize > 0) {
      System.out.println("(MAP-NODE contains " + mapNodeSize + " ENTRIES)");
    }
    System.out.println(headingBorder);
  }

  private class MultiKeyComponentEntry<V> {

    private KeyComponentArray keyComponentArray;
    private V value;

    public MultiKeyComponentEntry(KeyComponentArray keyComponentArray, V value) {
      this.keyComponentArray = keyComponentArray;
      this.value = value;
    }

    @SafeVarargs
    public MultiKeyComponentEntry(V value, Object... keyComponentArray) {
      this(new KeyComponentArray(keyComponentArray), value);
    }

    public MultiKeyComponentEntry(List<Object> keyComponentList, V value) {
      this(new KeyComponentArray(
              keyComponentList.toArray(new Object[keyComponentList.size()])), value);
    }

    public int getKeyComponentArrayLength() {
      return keyComponentArray.getLength();
    }

    protected void putTopKeyComponent(Object newTopKey) {
      keyComponentArray.putTopKeyComponent(newTopKey);
    }

    public Object getTopKeyComponent() {
      return keyComponentArray.getTopKeyComponent();
    }

    public KeyComponentArray getKeyComponentArray() {
      return this.keyComponentArray;
    }

    public List<Object> getKeyComponentList() {
      return Arrays.asList(this.keyComponentArray.arrayOfKeyComponents);
    }

    public V getValue() {
      return value;
    }

    public boolean containsNulls() {
      return (keyComponentArray.containsNulls() || value == null);
    }

    public MultiKeyComponentEntry<V> getLowerEntry() {
      return new MultiKeyComponentEntry<V>(
              keyComponentArray.getLowerKeyComponentArray(), value);
    }
  }

  private class KeyComponentArray implements Iterable<Object> {

    private Object[] arrayOfKeyComponents;

    @SafeVarargs
    public KeyComponentArray(Object... keyComponentArray) {
      this.arrayOfKeyComponents = keyComponentArray;
    }

    public int getLength() {
      return arrayOfKeyComponents.length;
    }

    protected void putTopKeyComponent(Object newTopKeyComponent) {
      Object[] newArray = new Object[arrayOfKeyComponents.length + 1];
      newArray[0] = newTopKeyComponent;
      for (int i = 1; i < newArray.length; i++) {
        newArray[i] = arrayOfKeyComponents[i - 1];
      }
      arrayOfKeyComponents = newArray;
    }

    public Object getTopKeyComponent() {
      if (arrayOfKeyComponents.length == 0) {
        return null;
      } else {
        return arrayOfKeyComponents[0];
      }
    }

    public boolean containsNulls() {
      for (Object key : arrayOfKeyComponents) {
        if (key == null) {
          return true;
        }
      }
      return false;
    }

    public KeyComponentArray getLowerKeyComponentArray() {
      if (arrayOfKeyComponents.length <= 1) {
        return new KeyComponentArray(new Object[0]); // empty array
      } else {
        return new KeyComponentArray(Arrays.copyOfRange(arrayOfKeyComponents, 1, arrayOfKeyComponents.length));
      }
    }

    @Override
    public Iterator<Object> iterator() {
      return (new ArrayList<Object>(
                      Arrays.asList(arrayOfKeyComponents))).iterator();
    }
  }

  private class HashCodeComparator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
      return o1.hashCode() - o2.hashCode();
    }
  }
}
