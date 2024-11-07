package moonlit.chill.ownpay.exception;

import cn.hutool.http.HttpStatus;
import lombok.Getter;

/**
 * @author Gawaind
 * @date 2024/11/5 16:21
 */
@Getter
public class PayException extends RuntimeException {

    private static final long serialVersionUID = 37146375433236756L;

    private final Integer code;

    private final String message;

    public PayException(String message) {
        this.message = message;
        this.code = HttpStatus.HTTP_INTERNAL_ERROR;
    }

    public PayException(Integer code, String message) {
        this.message = message;
        this.code = code;
    }
}
