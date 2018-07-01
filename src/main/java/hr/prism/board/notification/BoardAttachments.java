package hr.prism.board.notification;

import com.sendgrid.Attachments;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BoardAttachments extends Attachments {

    private String url;

    public String getUrl() {
        return url;
    }

    public BoardAttachments setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(url)
            .toHashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        BoardAttachments that = (BoardAttachments) object;
        return new EqualsBuilder()
            .append(url, that.url)
            .isEquals();
    }

}
