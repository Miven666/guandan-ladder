package com.guandan.ladder.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.guandan.ladder.constant.UnConfirmTypeEnum;
import com.guandan.ladder.model.convert.GameConverter;
import com.guandan.ladder.model.dto.ConfirmRecordDto;
import com.guandan.ladder.model.dto.GameRecordDto;
import com.guandan.ladder.model.dto.GameRecordOutDto;
import com.guandan.ladder.model.dto.GameRecordVO;
import com.guandan.ladder.model.entity.GameRecord;
import com.guandan.ladder.model.entity.User;
import com.guandan.ladder.security.SecurityContext;
import com.guandan.ladder.service.GameService;
import com.guandan.ladder.service.UserService;
import com.hccake.ballcat.common.model.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hccake
 */
@Slf4j
@RequestMapping("/game")
@RestController
@RequiredArgsConstructor
public class GameController {

	private final GameService gameService;

	private final UserService userService;

	/**
	 * 上报游戏记录
	 */
	@PostMapping("/record")
	public R<Void> reportGameRecord(@RequestBody @Validated GameRecordDto gameRecordDto) {
		Set<String> uids = Stream
			.of(gameRecordDto.getWinUid1(), gameRecordDto.getWinUid2(), gameRecordDto.getLoseUid1(),
					gameRecordDto.getLoseUid2())
			.collect(Collectors.toSet());
		Assert.isTrue(uids.size() == 4, "比赛记录的成员必须是 4 人： {}", uids);
		gameService.saveRecord(gameRecordDto);
		return R.ok();
	}

	/**
	 * 历史战绩
	 * @param uid 用户id
	 */
	@GetMapping("/list/{uid}")
	public R<List<GameRecordVO>> gameList(@PathVariable("uid") String uid) {
		List<GameRecord> gameRecords = gameService.gameList(uid);
		List<GameRecordVO> result = getGameRecordVoList(gameRecords);
		return R.ok(result);
	}

	/**
	 * 我的历史战绩
	 */
	@GetMapping("/list")
	public R<List<GameRecordVO>> gameMyList() {
		List<GameRecord> gameRecords = gameService.gameList(SecurityContext.getUserId());
		List<GameRecordVO> result = getGameRecordVoList(gameRecords);
		return R.ok(result);
	}

	/**
	 * 待生效战绩列表
	 * @param myOrAll 待确认查询范围
	 */
	@GetMapping("/record/unconfirmed")
	public R<List<GameRecordVO>> unconfirmedRecordList(@RequestParam("myOrAll") Integer myOrAll) {
		UnConfirmTypeEnum unConfirmTypeEnum = UnConfirmTypeEnum.valueOf(myOrAll);
		List<GameRecord> gameRecords = gameService.unconfirmedRecordList(unConfirmTypeEnum);
		log.info("待生效列表，{},{}", myOrAll, gameRecords);
		List<GameRecordVO> result = getGameRecordVoList(gameRecords);
		return R.ok(result);
	}

	private List<GameRecordVO> getGameRecordVoList(List<GameRecord> gameRecords) {
		Set<String> uids = new HashSet<>();
		for (GameRecord gameRecord : gameRecords) {
			uids.add(gameRecord.getWinUid1());
			uids.add(gameRecord.getWinUid2());
			uids.add(gameRecord.getLoseUid1());
			uids.add(gameRecord.getLoseUid2());
		}

		if (CollUtil.isEmpty(uids)) {
			return Collections.emptyList();
		}

		Map<String, User> userMap = userService.listUserMapByUids(uids);

		List<GameRecordVO> result = new ArrayList<>(gameRecords.size());
		for (GameRecord gameRecord : gameRecords) {
			GameRecordVO gameRecordVO = GameConverter.INSTANCE.recordEntityToVO(gameRecord);
			// 不足4位的二进制补成4位
			String flag = String.format("%4s", gameRecordVO.getUserConfirmFlag()).replace(' ', '0');
			gameRecordVO.setWinUid1Flag(flag.charAt(0));
			gameRecordVO.setWinUid2Flag(flag.charAt(1));
			gameRecordVO.setLoseUid1Flag(flag.charAt(2));
			gameRecordVO.setLoseUid2Flag(flag.charAt(3));
			gameRecordVO.setWinNickname1(userMap.get(gameRecordVO.getWinUid1()).getNickname());
			gameRecordVO.setWinNickname2(userMap.get(gameRecordVO.getWinUid2()).getNickname());
			gameRecordVO.setLoseNickname1(userMap.get(gameRecordVO.getLoseUid1()).getNickname());
			gameRecordVO.setLoseNickname2(userMap.get(gameRecordVO.getLoseUid2()).getNickname());
			result.add(gameRecordVO);
		}
		return result;
	}

	/**
	 * 战绩确认 4人都确认才生效
	 */
	@PostMapping("/confirm-record")
	public R<Void> confirmRecord(@RequestBody @Validated ConfirmRecordDto confirmRecordDto) {
		gameService.confirmRecord(confirmRecordDto);
		return R.ok();
	}

}
