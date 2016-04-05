/*
 * Copyright (C) 2015 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commonvox.collections;

import java.lang.reflect.Method;
import java.util.Comparator;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Daniel Vimont
 */
public class TestKeyComponentProfile {

  private static Method AUTHOR_GET_METHOD;
  private static Method TITLE_GET_METHOD;
  private static Method SUBTITLE_GET_METHOD;
  private static Method GENRE_GET_METHOD;
  private static Method NONCOMPARABLE_GET_METHOD;
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
              = Book.class.getDeclaredMethod("getNoncomparableObject");
    }
    catch (NoSuchMethodException ex) {
    }
  }
  private static final String KEY_COMPONENT_CLASS_FAILURE
            = "Failure of get/set keyComponentClass";
  private static final String KEY_COMPONENT_GETMETHODS_FAILURE
            = "Failure of get/set keyComponentGetMethods";
  private static final String KEY_COMPONENT_BASIS_FAILURE
            = "Failure of get/set keyComponentBasis";

  @Test
  public void testKeyComponentProfileConstructors ()
          throws NoSuchMethodException {

    KeyComponentProfile simpleClassComponent
            = new KeyComponentProfile<Book>(Book.class, Author.class);
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Author.class,
            simpleClassComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            1, simpleClassComponent.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            AUTHOR_GET_METHOD,
            simpleClassComponent.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.CLASS,
            simpleClassComponent.getKeyComponentBasis());

    // test KeyComponentProfile constructed with Comparator
    simpleClassComponent
            = new KeyComponentProfile<Book>(
                    Book.class, Author.class, new FirstNameComparator());
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Author.class,
            simpleClassComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            1, simpleClassComponent.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            AUTHOR_GET_METHOD,
            simpleClassComponent.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.CLASS,
            simpleClassComponent.getKeyComponentBasis());

    KeyComponentProfile simpleClassWithMultipleMethodsComponent
            = new KeyComponentProfile<Book>(Book.class, Book.Title.class);
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Book.Title.class,
            simpleClassWithMultipleMethodsComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            2, simpleClassWithMultipleMethodsComponent.
                    getKeyComponentGetMethods().size());
    assertTrue(KEY_COMPONENT_GETMETHODS_FAILURE,
            TITLE_GET_METHOD.equals(
                simpleClassWithMultipleMethodsComponent.
                        getKeyComponentGetMethods().get(0))
            || SUBTITLE_GET_METHOD.equals(
                simpleClassWithMultipleMethodsComponent.
                        getKeyComponentGetMethods().get(0)));
    assertTrue(KEY_COMPONENT_GETMETHODS_FAILURE,
            TITLE_GET_METHOD.equals(
                simpleClassWithMultipleMethodsComponent.
                        getKeyComponentGetMethods().get(1))
            || SUBTITLE_GET_METHOD.equals(
                simpleClassWithMultipleMethodsComponent.
                        getKeyComponentGetMethods().get(1)));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.CLASS,
            simpleClassWithMultipleMethodsComponent.getKeyComponentBasis());

    KeyComponentProfile simpleMethodComponent
            = new KeyComponentProfile<Book>(Book.class, Author.class,
                    AUTHOR_GET_METHOD);
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Author.class,
            simpleMethodComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            1, simpleMethodComponent.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            AUTHOR_GET_METHOD,
            simpleMethodComponent.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.METHOD,
            simpleMethodComponent.getKeyComponentBasis());

    KeyComponentProfile multiMethodComponent
            = new KeyComponentProfile<Book>(Book.class, Book.Title.class,
                    TITLE_GET_METHOD,
                SUBTITLE_GET_METHOD);
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Book.Title.class,
            multiMethodComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            2, multiMethodComponent.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            TITLE_GET_METHOD,
            multiMethodComponent.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            SUBTITLE_GET_METHOD,
            multiMethodComponent.getKeyComponentGetMethods().get(1));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.METHOD,
            multiMethodComponent.getKeyComponentBasis());

    // parameterless constructor is package-private
    KeyComponentProfile identityComponent = new KeyComponentProfile<Book>();
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Object.class, identityComponent.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            1, identityComponent.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            KeyComponentProfile.IDENTITY_METHOD,
            identityComponent.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.IDENTITY,
            identityComponent.getKeyComponentBasis());

    KeyComponentProfile identityComponent2
            = new KeyComponentProfile<Book>(Book.class, Book.class);
    assertEquals(KEY_COMPONENT_CLASS_FAILURE,
            Book.class, identityComponent2.getKeyComponentClass());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            1, identityComponent2.getKeyComponentGetMethods().size());
    assertEquals(KEY_COMPONENT_GETMETHODS_FAILURE,
            KeyComponentProfile.IDENTITY_METHOD,
            identityComponent2.getKeyComponentGetMethods().get(0));
    assertEquals(KEY_COMPONENT_BASIS_FAILURE,
            KeyComponentProfile.KeyComponentBasis.IDENTITY,
            identityComponent2.getKeyComponentBasis());
  }

  @Test(expected=IllegalArgumentException.class)
  public void testWithInaccessibleAttribute() {
    KeyComponentProfile simpleClassComponent
            = new KeyComponentProfile<Book>(Book.class,
                    InaccessibleAttribute.class);
  }

  public void testWithNoncomparableAttribute() {
    KeyComponentProfile simpleClassComponent
            = new KeyComponentProfile<Book>(
                    Book.class, Book.NoncomparableTitle.class,
                    NONCOMPARABLE_GET_METHOD);
  }

  class FirstNameComparator implements Comparator<Author> {
    @Override
    public int compare(Author o1, Author o2) {
      return o1.firstName.compareTo(o2.firstName);
    }
  }
  class StringComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
      return o1.compareTo(o2);
    }
  }
}
