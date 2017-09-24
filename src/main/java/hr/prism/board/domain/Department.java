package hr.prism.board.domain;

import hr.prism.board.enums.Scope;

import javax.persistence.*;
import java.util.Objects;

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

    @Column(name = "member_count_pending")
    private Long memberCountPending;

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

    public Long getMemberCountPending() {
        return memberCountPending;
    }

    public void setMemberCountPending(Long memberCountPending) {
        this.memberCountPending = memberCountPending;
    }

    public void addToMemberCountPending(Long memberCountPending) {
        if (this.memberCountPending == null) {
            this.memberCountPending = memberCountPending;
        } else {
            this.memberCountPending = this.memberCountPending + memberCountPending;
        }
    }

    public void decrementMemberCountPending() {
        if (Objects.equals(this.memberCountPending, 1L)) {
            this.memberCountPending = null;
        } else {
            this.memberCountPending = this.memberCountPending - 1L;
        }
    }

}
