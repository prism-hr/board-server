package hr.prism.board.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.domain.ResourceCategory;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.PostRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostMapper implements Function<Post, PostRepresentation> {
    
    @Inject
    private LocationMapper locationMapper;
    
    @Inject
    private DocumentMapper documentMapper;
    
    @Inject
    private BoardMapper boardMapper;
    
    @Inject
    private ResourceMapper resourceMapper;
    
    @Inject
    private ObjectMapper objectMapper;
    
    @Override
    public PostRepresentation apply(Post post) {
        if (post == null) {
            return null;
        }
        
        return resourceMapper.apply(post, PostRepresentation.class)
            .setDescription(post.getDescription())
            .setOrganizationName(post.getOrganizationName())
            .setLocation(locationMapper.apply(post.getLocation()))
            .setExistingRelation(post.getExistingRelation())
            .setExistingRelationExplanation(mapExistingRelationExplanation(post.getExistingRelationExplanation()))
            .setPostCategories(post.getPostCategories().stream().map(ResourceCategory::getName).collect(Collectors.toList()))
            .setMemberCategories(post.getMemberCategories().stream().map(ResourceCategory::getName).collect(Collectors.toList()))
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyEmail(post.getApplyEmail())
            .setBoard(boardMapper.apply((Board) post.getParent()))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp());
    }
    
    private LinkedHashMap<String, Object> mapExistingRelationExplanation(String existingRelationExplanation) {
        if (existingRelationExplanation == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(existingRelationExplanation, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (IOException e) {
            throw new ApiException(ExceptionCode.CORRUPTED_POST_EXISTING_RELATION_EXPLANATION, e);
        }
    }
    
}
