package hr.prism.board.representation;

import com.google.common.base.MoreObjects;

import java.util.LinkedList;
import java.util.Objects;

public class ChangeListRepresentation extends LinkedList<ChangeListRepresentation.ChangeRepresentation> {

    public ChangeListRepresentation put(String property, Object oldValue, Object newValue) {
        super.add(new ChangeRepresentation().setProperty(property).setOldValue(oldValue).setNewValue(newValue));
        return this;
    }

    public static class ChangeRepresentation {

        private String property;

        private Object oldValue;

        private Object newValue;

        public String getProperty() {
            return property;
        }

        public ChangeRepresentation setProperty(String property) {
            this.property = property;
            return this;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public ChangeRepresentation setOldValue(Object oldValue) {
            this.oldValue = oldValue;
            return this;
        }

        public Object getNewValue() {
            return newValue;
        }

        public ChangeRepresentation setNewValue(Object newValue) {
            this.newValue = newValue;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(property, oldValue, newValue);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChangeRepresentation that = (ChangeRepresentation) o;
            return Objects.equals(property, that.property) &&
                Objects.equals(oldValue, that.oldValue) &&
                Objects.equals(newValue, that.newValue);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("property", property)
                .add("oldValue", oldValue)
                .add("newValue", newValue)
                .toString();
        }

    }

}
