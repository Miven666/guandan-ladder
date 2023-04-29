package com.guandan.ladder.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 待确认归属范围枚举
 * @author hushaoming
 *  2023/4/29 13:47
 */
@Getter
@RequiredArgsConstructor
public enum UnConfirmTypeEnum {

	MY(1, "我的待确认"),

	ALL(2, "所有待确认");

	private final Integer value;

	private final String desc;

	public static UnConfirmTypeEnum valueOf(Integer value) {
		for (UnConfirmTypeEnum rankListTypeEnum : UnConfirmTypeEnum.values()) {
			if (rankListTypeEnum.value.equals(value)) {
				return rankListTypeEnum;
			}
		}
		return null;
	}

}
