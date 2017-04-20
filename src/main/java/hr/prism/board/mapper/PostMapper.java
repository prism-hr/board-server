package hr.prism.board.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.prism.board.domain.Board;
import hr.prism.board.domain.Post;
import hr.prism.board.enums.CategoryType;
import hr.prism.board.exception.ApiException;
import hr.prism.board.exception.ExceptionCode;
import hr.prism.board.representation.PostRepresentation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

@Service
public class PostMapper implements Function<Post, PostRepresentation> {

    @Inject
    private LocationMapper locationMapper;

    @Inject
    private DocumentMapper documentMapper;

    @Inject
    private BoardMapper boardMapper;

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public PostRepresentation apply(Post post) {
        if (post == null) {
            return null;
        }

        Board board = (Board) post.getParent();
        List<String> postCategories = new ArrayList<>();
        List<String> memberCategories = new ArrayList<>();
        post.getCategories().forEach(category -> {
            if (category.getType() == CategoryType.POST) {
                postCategories.add(category.getName());
            } else {
                memberCategories.add(category.getName());
            }
        });

        PostRepresentation postRepresentation = new PostRepresentation();
        postRepresentation
            .setId(post.getId())
            .setScope(post.getScope())
            .setName(post.getName())
            .setState(post.getState());
        postRepresentation
            .setDescription(post.getDescription())
            .setOrganizationName(post.getOrganizationName())
            .setLocation(locationMapper.apply(post.getLocation()))
            .setExistingRelation(post.getExistingRelation())
            .setExistingRelationExplanation(mapExistingRelationExplanation(post.getExistingRelationExplanation()))
            .setPostCategories(postCategories)
            .setMemberCategories(memberCategories)
            .setApplyWebsite(post.getApplyWebsite())
            .setApplyDocument(documentMapper.apply(post.getApplyDocument()))
            .setApplyEmail(post.getApplyEmail())
            .setBoard(boardMapper.apply(board))
            .setLiveTimestamp(post.getLiveTimestamp())
            .setDeadTimestamp(post.getDeadTimestamp());

        postRepresentation.setActions(post.getActions());
        return postRepresentation;
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
