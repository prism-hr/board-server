package hr.prism.board.service;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class LocalizationService {
    
    @Inject
    private MessageSource messageSource;
    
    public String getMessage(List<String> parts, Locale locale) {
        return WordUtils.capitalize(Joiner.on(" ").join(parts.stream().map(part -> messageSource.getMessage(part, null, locale)).collect(Collectors.toList()))) + ".";
    }
    
}
