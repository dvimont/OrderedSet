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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An array of <i>KeyComponentProfile</i> objects specifies the composite-key
 * structure which orders the elements in an {@link OrderedSet}.
 * The construction of a <i>KeyComponentProfile</i> establishes how its
 * corresponding <i>keyComponentClass</i>-objects are to be automatically retrieved
 * and ordered when <i>valueClass</i>-elements are added to an {@link OrderedSet}
 * to which the <i>KeyComponentProfile</i> belongs.
 * A single <i>KeyComponentProfile</i> object may be used in multiple
 * {@link OrderedSet}s.
 * <br><br>
 * Usage examples can be found
 * <b><a href="./OrderedSet.html#usage_examples">here</a></b>.
 *
 * @author Daniel Vimont
 * @param <V> The <i>valueClass</i>, i.e. the class of elements contained in
 * the {@link OrderedSet} to which the <i>KeyComponentProfile</i>
 * belongs.
 * The <i>valueClass</i> must match the {@literal <V>} type-parameter
 * of any {@link OrderedSet} to which the <i>KeyComponentProfile</i>
 * belongs (enforced at compile-time).
 */
public class KeyComponentProfile<V> implements Serializable {

  private final Class<V> valueClass;
  private final Class<?> keyComponentClass;
  private final List<Method> valueClassMethodsThatReturnKeyComponents;
  private final KeyComponentBasis indexComponentBasis;
  private final String indexComponentName;
  private final Comparator<?> keyComponentClassComparator;
  private final int immutableHashCode;
  static final Method IDENTITY_METHOD;
  static final String INVALID_METHOD_MESSAGE_OPENER =
          "Invalid method(s) submitted in the KeyComponentProfile's "
          + "methodsThatReturnKeyComponents array: ";

  static enum KeyComponentBasis {

    CLASS, METHOD, IDENTITY // , FIELD

  };

  static {
    Method dummyMethod;
    try {
      dummyMethod = GetThisObject.class.getMethod("getThis");
    }
    catch (NoSuchMethodException e) {
      throw new InternalError("Internal coding error has resulted in failure "
              + "to set IDENTITY_METHOD constant in <"
              + KeyComponentProfile.class.getSimpleName() + "> class.");
    }
    IDENTITY_METHOD = dummyMethod;
  }

  /**
   * THIS PACKAGE-PRIVATE CONSTRUCTOR USED ONLY INTERNALLY BY MapNode class!
   * Constructs an IDENTITY <i>KeyComponentProfile</i> for the
   * {@link OrderedSet} to which it belongs;
   * the ordering maintained by such a <i>KeyComponentProfile</i> is based
   * upon the <i>valueClass</i> itself (as stipulated by the
   * {@literal <V>} type-parameter).
   */
  KeyComponentProfile() {
    this.indexComponentBasis = KeyComponentBasis.IDENTITY;
    this.valueClass = null;
    this.indexComponentName = KeyComponentBasis.IDENTITY.toString();
    this.keyComponentClass = Object.class;
    this.valueClassMethodsThatReturnKeyComponents = new ArrayList<Method>();
    this.valueClassMethodsThatReturnKeyComponents.add(IDENTITY_METHOD);
    this.keyComponentClassComparator = null;
    this.immutableHashCode = computeImmutableHashCode();
  }

  /**
   * Constructs a <i>KeyComponentProfile</i> belonging to an
   * {@link OrderedSet} containing elements of
   * class <i>valueClass</i>;
   * the ordering maintained by a <i>KeyComponentProfile</i> is stipulated
   * by the <i>keyComponentClass</i> parameter, which must either be the same
   * as the <i>valueClass</i> or must correspond to a class of
   * objects returned by one or more of the <i>valueClass</i>'s parameterless
   * "get" methods; submission of <i>methodsReturningKeyComponents</i> vararg
   * parameters is optional.
   * <br><br>
   * Usage examples can be found
   * <b><a href="./OrderedSet.html#usage_examples">here</a></b>.
   * <br><br>
   * <b>CONSTRUCTION</b>
   * <br>
   * During construction of the <i>KeyComponentProfile</i>, automatic
   * assembly of an internal <i>valueClassMethodsThatReturnKeyComponents</i>
   * array is done via Java reflection. Any non-private, non-static,
   * parameterless method of the <i>valueClass</i> which returns either a
   * single instance of a <i>keyComponentClass</i>-object or a Collection of
   * <i>keyComponentClass</i>-objects is added to the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array.
   * A subset of these methods may optionally be stipulated by the programmer
   * via the optional <i>methodsReturningKeyComponents</i> vararg parameters;
   * if one or more of these vararg parameters are submitted, the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array is limited to this
   * subset of the <i>valueClass</i> "get" methods.
   * <br><br>
   * <b>USAGE IN AN {@link OrderedSet}</b>
   * <br>
   * Subsequent to <i>KeyComponentProfile</i> construction, as
   * <i>valueClass</i>-elements are
   * {@link OrderedSet#add(java.lang.Object) add}ed
   * to an {@link OrderedSet}
   * to which the <i>KeyComponentProfile</i> belongs,
   * <i>keyComponentClass</i>-objects are automatically retrieved and ordered.
   * If the <i>keyComponentClass</i> equals the <i>valueClass</i>, then the
   * <i>valueClass</i>-element itself serves the role of
   * <i>keyComponentClass</i>-object. Otherwise,
   * <i>keyComponentClass</i>-objects are retrieved via reflection-based
   * invocation of the "get" methods in the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array.
   * All retrieved <i>keyComponentClass</i>-objects are internally ordered via backing
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html">
   * TreeMap</a>s, either in
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html">
   * natural order</a> or
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--">
   * hashCode order</a>.
   * If the <i>keyComponentClass</i> implements the
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html"
   * target="_blank">Comparable</a> interface, its objects are ordered
   * in <i>natural order</i>; otherwise they are ordered in <i>hashCode order</i>.
   *
   * @param <K> keyComponentClass
   * @param valueClass The class of elements contained in the
   * {@link OrderedSet} to which the <i>KeyComponentProfile</i>
   * belongs.
   * This value must match that of the {@literal <V>} type-parameter
   * (enforced at compile-time).
   * @param keyComponentClass The class of the objects to be retrieved and
   * ordered by the <i>KeyComponentProfile</i>.
   * The <i>keyComponentClass</i> must either be the same
   * as the <i>valueClass</i> or must correspond to a class of
   * objects returned by one or more of the <i>valueClass</i>'s parameterless
   * "get" methods.
   * @param methodsReturningKeyComponents an optional subset of the
   * <i>valueClass</i>'s parameterless "get" methods; if submitted, keyComponent
   * reflection-based retrieval (as described above) will be limited to
   * invocation of this subset of methods.
   */
  public <K> KeyComponentProfile(Class<V> valueClass,
          Class<K> keyComponentClass,
          Method... methodsReturningKeyComponents) {
    this(valueClass, keyComponentClass, null, methodsReturningKeyComponents);
  }

  /**
   * Constructs a <i>KeyComponentProfile</i> belonging to an
   * {@link OrderedSet} containing elements of
   * class <i>valueClass</i>;
   * the ordering maintained by a <i>KeyComponentProfile</i> is stipulated
   * by the <i>keyComponentClass</i> parameter, which must either be the same
   * as the <i>valueClass</i> or must correspond to a class of
   * objects returned by one or more of the <i>valueClass</i>'s parameterless
   * "get" methods; submission of <i>methodsReturningKeyComponents</i> vararg
   * parameters is optional.
   * <br><br>
   * Usage examples can be found
   * <b><a href="./OrderedSet.html#usage_examples">here</a></b>.
   * <br><br>
   * <b>CONSTRUCTION</b>
   * <br>
   * During construction of the <i>KeyComponentProfile</i>, automatic
   * assembly of an internal <i>valueClassMethodsThatReturnKeyComponents</i>
   * array is done via Java reflection. Any non-private, non-static,
   * parameterless method of the <i>valueClass</i> which returns either a
   * single instance of a <i>keyComponentClass</i>-object or a Collection of
   * <i>keyComponentClass</i>-objects is added to the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array.
   * A subset of these methods may optionally be stipulated by the programmer
   * via the optional <i>methodsReturningKeyComponents</i> vararg parameters;
   * if one or more of these vararg parameters are submitted, the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array is limited to this
   * subset of the <i>valueClass</i> "get" methods.
   * <br><br>
   * <b>USAGE IN AN {@link OrderedSet}</b>
   * <br>
   * Subsequent to <i>KeyComponentProfile</i> construction, as
   * <i>valueClass</i>-elements are
   * {@link OrderedSet#add(java.lang.Object) add}ed
   * to an {@link OrderedSet}
   * to which the <i>KeyComponentProfile</i> belongs,
   * <i>keyComponentClass</i>-objects are automatically retrieved and ordered.
   * If the <i>keyComponentClass</i> equals the <i>valueClass</i>, then the
   * <i>valueClass</i>-element itself serves the role of
   * <i>keyComponentClass</i>-object. Otherwise,
   * <i>keyComponentClass</i>-objects are retrieved via reflection-based
   * invocation of the "get" methods in the
   * <i>valueClassMethodsThatReturnKeyComponents</i> array.
   * The retrieved <i>keyComponentClass</i>-objects are then ordered using a
   * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html"
   * target="_blank">TreeMap</a> structure, placing the
   * <i>keyComponentClass</i>-objects in the order maintained by the
   * Comparator stipulated in the <i>keyComponentClassComparator</i> parameter.
   *
   * @param <K> keyComponentClass
   * @param valueClass The class of elements contained in the
   * {@link OrderedSet} to which the <i>KeyComponentProfile</i>
   * belongs.
   * This value must match that of the {@literal <V>} type-parameter
   * (enforced at compile-time).
   * @param keyComponentClass The class of the objects to be retrieved and
   * ordered by the <i>KeyComponentProfile</i>.
   * The <i>keyComponentClass</i> must either be the same
   * as the <i>valueClass</i> or must correspond to a class of
   * objects returned by one or more of the <i>valueClass</i>'s parameterless
   * "get" methods.
   * @param keyComponentClassComparator Comparator to be used for ordering
   * retrieved <i>keyComponentClass</i>-objects.
   * @param methodsReturningKeyComponents an optional subset of the
   * <i>valueClass</i>'s parameterless "get" methods; if submitted, keyComponent
   * reflection-based retrieval (as described above) will be limited to
   * invocation of this subset of methods.
   */
  public <K> KeyComponentProfile(Class<V> valueClass, Class<K> keyComponentClass,
          Comparator<? super K> keyComponentClassComparator,
          Method... methodsReturningKeyComponents) {
    this.valueClass = valueClass;
    this.keyComponentClass = keyComponentClass;
    this.keyComponentClassComparator = keyComponentClassComparator;
    StringBuilder nameBuilder = new StringBuilder();
    nameBuilder.append(this.keyComponentClass.getSimpleName());
    if (this.valueClass.equals(this.keyComponentClass)) {
      this.indexComponentBasis = KeyComponentBasis.IDENTITY;
      this.valueClassMethodsThatReturnKeyComponents = new ArrayList<Method>();
      this.valueClassMethodsThatReturnKeyComponents.add(IDENTITY_METHOD);
    } else {
      if (methodsReturningKeyComponents == null
            || methodsReturningKeyComponents.length == 0) {
        this.indexComponentBasis = KeyComponentBasis.CLASS;
        this.valueClassMethodsThatReturnKeyComponents
                = getMethodsThatReturnObjectsOfKeyComponentClass();
        if (this.valueClassMethodsThatReturnKeyComponents.isEmpty()) {
          throw new IllegalArgumentException("Invalid keyComponentClass, <"
                  + this.keyComponentClass.getSimpleName() + ">, submitted for "
                  + "construction of KeyComponentProfile. No parameterless "
                  + "method returning object of this keyComponentClass "
                  + "(or Collection of objects of this keyComponentClass) "
                  + "is declared by class (or superclass of) valueClass, <"
                  + this.valueClass.getSimpleName() + ">");
        }
      } else {
        this.indexComponentBasis = KeyComponentBasis.METHOD;
        this.valueClassMethodsThatReturnKeyComponents =
                Arrays.asList(methodsReturningKeyComponents);
        if (this.keyComponentClass != getClassOfObjectsReturnedByMethods()) {
          throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                  + "method(s) return object(s) of class <"
                  + getClassOfObjectsReturnedByMethods().getSimpleName()
                  + "> which differs from submitted keyComponentClass <"
                  + this.keyComponentClass.getSimpleName() + ">");
        }
      }
      for (Method method : this.valueClassMethodsThatReturnKeyComponents) {
        nameBuilder.append(";").append(method.getName());
      }
    }
    this.indexComponentName = nameBuilder.toString();
    this.immutableHashCode = computeImmutableHashCode();
  }

  private List<Method> getMethodsThatReturnObjectsOfKeyComponentClass() {
    List<Method> methodSet = new ArrayList<Method>();
    // examine all declared and inherited non-static PUBLIC methods
    for (Method method : this.valueClass.getMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      if (methodIsParameterlessAndReturnsObjectsOfKeyComponentClass(method)) {
        methodSet.add(method);
      }
    }
    // examine all declared non-static PROTECTED and PACKAGE-PRIVATE methods
    for (Method method : this.valueClass.getDeclaredMethods()) {
      int modifiers = method.getModifiers();
      if (Modifier.isStatic(modifiers) || Modifier.isPublic(modifiers)
              || Modifier.isPrivate(modifiers)) {
        continue;
      }
      if (methodIsParameterlessAndReturnsObjectsOfKeyComponentClass(method)) {
        method.setAccessible(true);
        methodSet.add(method);
      }
    }
    return methodSet;
  }

  private boolean methodIsParameterlessAndReturnsObjectsOfKeyComponentClass(Method method) {
    if (method.getParameterCount() > 0) {
      return false;
    }
    if (method.getReturnType().equals(this.keyComponentClass)) {
      return true;
    } else if (Collection.class.isAssignableFrom(method.getReturnType())) {
      ParameterizedType collectionType
              = (ParameterizedType) method.getGenericReturnType();
      Type typeOfObjectsInCollection
              = collectionType.getActualTypeArguments()[0];
      if (typeOfObjectsInCollection.getTypeName().equals(
              this.keyComponentClass.getTypeName())) {
        return true;
      }
    }
    return false;
  }

  private Class<?> getClassOfObjectsReturnedByMethods() {
    Class returnedClass = null;

    for (Method method : this.valueClassMethodsThatReturnKeyComponents) {
      if (method == null) {
        throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                + "<null>.");
      }
      if (!method.getDeclaringClass().isAssignableFrom(this.valueClass)) {
        throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                + "<" + method.getName() + "> method's declaring class is <"
                + method.getDeclaringClass().getSimpleName()
                + ">, which is not assignable from valueClass, <"
                + this.valueClass.getSimpleName() + ">.");
      }
      if (method.getParameterCount() > 0) {
        throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                + "<" + method.getName()
                + "> method is not parameterless as required.");
      }
      if (Collection.class.isAssignableFrom(method.getReturnType())) {
        ParameterizedType collectionType
                = (ParameterizedType) method.getGenericReturnType();
        Type typeOfObjectsInCollection
                = collectionType.getActualTypeArguments()[0];
        try {
          Class classInCollection
                  = Class.forName(typeOfObjectsInCollection.getTypeName());
          if (returnedClass == null) {
            returnedClass = classInCollection;
          } else if (!returnedClass.equals(classInCollection)) {
            throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                    + "<" + method.getName()
                    + "> method returns Collection of objects of class "
                    + "which differs from class of object returned by "
                    + "other method(s) in the KeyComponentProfile's "
                    + "<methodsThatReturnKeyComponents> array.");
          }
        }
        catch (ClassNotFoundException e) {
          throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                  + "<" + method.getName()
                  + "> method returns Collection of objects "
                  + "belonging to a Class that cannot be found.", e);
        }
      } else {
        if (returnedClass == null) {
          returnedClass = method.getReturnType();
        } else if (!returnedClass.equals(method.getReturnType())) {
          throw new IllegalArgumentException(INVALID_METHOD_MESSAGE_OPENER
                  + "<" + method.getName()
                  + "> method returns object of class which differs from "
                  + "class of object returned by other method(s) in the "
                  + "KeyComponentProfile's "
                  + "<methodsThatReturnKeyComponents> array.");
        }
      }
    }
    return returnedClass;
  }

  KeyComponentBasis getKeyComponentBasis() {
    return this.indexComponentBasis;
  }

  Class<?> getKeyComponentClass() {
    return this.keyComponentClass;
  }

  Comparator getKeyComponentClassComparator() {
    return this.keyComponentClassComparator;
  }

  List<Method> getKeyComponentGetMethods() {
    return this.valueClassMethodsThatReturnKeyComponents;
  }

  private int computeImmutableHashCode() {
    int hash = 5;
    hash = 13 * hash
            + (this.valueClass != null ? this.valueClass.hashCode() : 0);
    hash = 13 * hash + (this.keyComponentClass != null ? this.keyComponentClass.hashCode() : 0);
    hash = 13 * hash + (this.valueClassMethodsThatReturnKeyComponents != null ?
            this.valueClassMethodsThatReturnKeyComponents.hashCode() : 0);
    hash = 13 * hash + (this.indexComponentBasis != null ?
            this.indexComponentBasis.hashCode() : 0);
    hash = 13 * hash + (this.keyComponentClassComparator != null ?
            this.keyComponentClassComparator.hashCode() : 0);
    return hash;
  }

  @Override
  public int hashCode() {
    return immutableHashCode;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (this.getClass() != other.getClass()) {
      return false;
    }
    return this.immutableHashCode ==
            ((KeyComponentProfile<?>)other).immutableHashCode;
  }

//  public int compareTo(KeyComponentProfile o) {
//    return this.immutableHashCode - o.immutableHashCode;
//  }

  /**
   * Returns a String representation of this object. The String representation
   * consists of the <i>KeyComponentProfile</i>'s basis (either CLASS, METHOD, or
   * IDENTITY) and the name of its basis entity.
   * @return a String representation of this object
   */
  @Override
  public String toString() {
    return "<KeyComponentProfile:basis==" + this.indexComponentBasis
            + " name==[" + this.indexComponentName + "]>";
  }

  Set<Object> getKeyComponentSet(V masterObject) {
    Set<Object> keyComponentSet = new HashSet<Object>();

    for (Method currentGetMethod : getKeyComponentGetMethods()) {
      if (Collection.class.isAssignableFrom(currentGetMethod.getReturnType())) {
        try {
          @SuppressWarnings("unchecked")
          Collection<Object> retrievedKeyCollection
                  = (Collection<Object>) currentGetMethod.invoke(masterObject);
          if (retrievedKeyCollection != null) {
            keyComponentSet.addAll(retrievedKeyCollection);
          }
        }
        catch (InvocationTargetException e) {
            throwMapNodePopulationInternalError(e);
        }
        catch (IllegalAccessException e) {
            throwMapNodePopulationInternalError(e);
        }

      } else {
        Object retrievedKey = null;
        if (getKeyComponentBasis().
                          equals(KeyComponentProfile.KeyComponentBasis.IDENTITY)) {
          retrievedKey = masterObject;
        } else {
          try {
            retrievedKey = currentGetMethod.invoke(masterObject);
          }
          catch (InvocationTargetException e) {
            throwMapNodePopulationInternalError(e);
          }
          catch (IllegalAccessException e) {
            throwMapNodePopulationInternalError(e);
          }
        }
        if (retrievedKey != null) {
          keyComponentSet.add(retrievedKey);
        }
      }
    }
    return keyComponentSet;
  }

  private void throwMapNodePopulationInternalError(Exception e) {
    throw new InternalError("Unanticipated " + e.getClass().getSimpleName()
            + " encountered while populating internal "
            + MapNode.class.getSimpleName() + ".", e);
  }

  /**
   * This interface used for IDENTITY_METHOD
   */
  private interface GetThisObject {

    public void getThis();

  }
}
