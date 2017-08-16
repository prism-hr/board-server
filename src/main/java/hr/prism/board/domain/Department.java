package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = Scope.Value.DEPARTMENT)
@NamedEntityGraph(
    name = "department.extended",
    attributeNodes = {
        @NamedAttributeNode(value = "categories"),
        @NamedAttributeNode(value = "documentLogo")})
public class Department extends Resource {

    @Column(name = "board_count")
    private Long boardCount;

    @Column(name = "member_count")
    private Long memberCount;

    public Long getBoardCount() {
        return boardCount;
    }

    public void setBoardCount(Long boardCount) {
        this.boardCount = boardCount;
    }

    public Long getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Long memberCount) {
        this.memberCount = memberCount;
    }

}
