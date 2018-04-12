package hr.prism.board.enums;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public enum PasswordHash {

    MD5,
    SHA256;

    public boolean matches(String input, String target) {
        switch (this) {
            case MD5:
                return md5Hex(input).equals(target);
            case SHA256:
                return sha256Hex(input).equals(target);
        }

        return false;
    }

}
