package hr.prism.board.patch;

import hr.prism.board.domain.Resource;

import java.util.Objects;
import java.util.Optional;

public class Patcher<T> {
    
    private Validator<T> validator;
    
    private Getter<T> getter;
    
    private Setter<T> setter;
    
    private Processor<T> processor;
    
    public static Patcher instance() {
        return new Patcher();
    }
    
    public Patcher<T> validator(Validator<T> validator) {
        this.validator = validator;
        return this;
    }
    
    public Patcher<T> getter(Getter<T> getter) {
        this.getter = getter;
        return this;
    }
    
    public Patcher<T> setter(Setter<T> setter) {
        this.setter = setter;
        return this;
    }
    
    public Patcher<T> setProcessor(Processor<T> processor) {
        this.processor = processor;
        return this;
    }
    
    public void patch(Resource resource, String property, Optional<T> newValueOptional) {
        if (newValueOptional != null) {
            T oldValue = this.getter.get();
            T newValue = newValueOptional.orElse(null);
            if (validator != null) {
                validator.execute(resource, newValue);
            }
            
            if (!Objects.equals(oldValue, newValue)) {
                this.setter.set(newValue);
                resource.getChangeList().put(property, oldValue, newValue);
            }
            
            if (processor != null) {
                processor.execute(resource, newValue);
            }
        }
    }
    
    public interface Validator<T> {
        void execute(Resource resource, T value);
    }
    
    public interface Getter<T> {
        T get();
    }
    
    public interface Setter<T> {
        void set(T value);
    }
    
    public interface Processor<T> {
        void execute(Resource resource, T value);
    }
    
}
