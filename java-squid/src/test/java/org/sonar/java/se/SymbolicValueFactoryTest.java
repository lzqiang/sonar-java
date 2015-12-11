/*
 * SonarQube Java
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.se;

import org.junit.Test;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.java.model.expression.IdentifierTreeImpl;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.SyntaxTrivia;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SymbolicValueFactoryTest {

  private static class TestSymbolicValue extends SymbolicValue {

    public TestSymbolicValue(int id) {
      super(id);
    }
  }

  private static class TestSymbolicValueFactory implements SymbolicValueFactory {

    @Override
    public SymbolicValue createSymbolicValue(int id, Tree syntaxNode) {
      return new TestSymbolicValue(id);
    }
  }

  @Test
  public void testFactory() {
    final IdentifierTree tree = new IdentifierTreeImpl(new InternalSyntaxToken(1, 1, "id", Collections.<SyntaxTrivia>emptyList(), 0, 0, false));
    final ConstraintManager manager = new ConstraintManager();
    SymbolicValue symbolicValue = manager.createSymbolicValue(tree);
    assertThat(symbolicValue.getClass()).as("Created without factory").isSameAs(SymbolicValue.class);
    manager.setValueFactory(new TestSymbolicValueFactory());
    symbolicValue = manager.createSymbolicValue(tree);
    assertThat(symbolicValue.getClass()).as("Created with factory").isSameAs(TestSymbolicValue.class);
    manager.setValueFactory(new TestSymbolicValueFactory());
    try {
      manager.setValueFactory(new TestSymbolicValueFactory());
      fail("Able to add a second factory to the contraints manager");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).as("Exception message").isEqualTo("The symbolic value factory has already been defined by another checker!");
    }
    symbolicValue = manager.createSymbolicValue(tree);
    assertThat(symbolicValue.getClass()).as("Created with first factory").isSameAs(TestSymbolicValue.class);
    symbolicValue = manager.createSymbolicValue(tree);
    assertThat(symbolicValue.getClass()).as("Created after factory usage").isSameAs(SymbolicValue.class);
  }
}
