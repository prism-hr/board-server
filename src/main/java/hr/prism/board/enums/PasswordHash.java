package hr.prism.board.enums;

import org.apache.commons.codec.digest.DigestUtils;

public enum PasswordHash {

    MD5,
    SHA256;

    public boolean matches(String input, String target) {
        switch (this) {
            case MD5:
                return DigestUtils.md5Hex(input).equals(target);
            case SHA256:
                return DigestUtils.sha256Hex(input).equals(target);
        }

        return false;
    }

}
