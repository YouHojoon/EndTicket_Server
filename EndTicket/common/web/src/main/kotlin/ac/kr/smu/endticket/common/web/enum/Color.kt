package ac.kr.smu.endticket.common.web.enum

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 색
 * @property value 색에 맞는 16진수 값
 */
@Schema(description = "색")
enum class Color(val value: String) {
    RED1("#E591A6"), RED2("#E28089"), RED3("#C56859"),
    ORANGE1("#EFCB7E"), ORANGE2("#EABD97"), ORANGE3("#E99D7B"),
    GREEN1("#88C7B2"), GREEN2("#83ABA5"), GREEN3("#4CA199"),
    BLUE1("#8DD3E8"), BLUE2("#7FBAD5"), BLUE3("#6D98DE"),
    PURPLE1("#B0BAF0"), PURPLE2("#A49CDA"), PURPLE3("#9F7E99"),
    GRAY1("#C2C8CF"), GRAY2("#A3A8B3"), GRAY3("#616871")
}