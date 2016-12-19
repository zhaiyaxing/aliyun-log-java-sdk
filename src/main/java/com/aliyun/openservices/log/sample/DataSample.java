package com.aliyun.openservices.log.sample;

import java.util.List;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogGroupData;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.Consts.CursorMode;
import com.aliyun.openservices.log.common.Shard;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.response.GetCursorResponse;
import com.aliyun.openservices.log.response.BatchGetLogResponse;
import com.aliyun.openservices.log.response.ListShardResponse;

class ClientSample {
	private final String endPoint = "";
	private final String akId = "your_access_id";
	private final String ak = "your_access_key";
	private final Client client = new Client(endPoint, akId, ak);
	private final String project = "your_project_name";
	private final String logStore = "your_log_store";
	private final int defaultShardNum = 10;

	public ClientSample() {
	}

	public void GetCursor() {
		int shardId = 1;
		GetCursorResponse res;
		try {
			long fromTime = (int)(System.currentTimeMillis()/1000.0 - 3600);
			res = client.GetCursor(project, logStore, shardId, fromTime);
			System.out.println("Cursor:" + res.GetCursor());

			res = client
					.GetCursor(project, logStore, shardId, CursorMode.BEGIN);
			System.out.println("Cursor:" + res.GetCursor());

			res = client.GetCursor(project, logStore, shardId, CursorMode.END);
			System.out.println("shard_id:" + shardId + " Cursor:" + res.GetCursor());
		} catch (LogException e) {
			e.printStackTrace();
		}
	}

	public void BatchGetLog() {
		try {
			for (int i = 0; i < defaultShardNum; i++) {
				GetCursorResponse cursorRes = client.GetCursor(project,
						logStore, i, CursorMode.BEGIN);
				String cursor = cursorRes.GetCursor();
				for (int j = 0; j < 5; j++) {
					BatchGetLogResponse logDataRes = client.BatchGetLog(
							project, logStore, i, 5, cursor);

					String next_cursor = logDataRes.GetNextCursor();
					System.out.print("The Next cursor:" + next_cursor);

					List<LogGroupData> logGroups = logDataRes.GetLogGroups();
					for (LogGroupData logGroup : logGroups) {
						System.out.println("Source:" + logGroup.GetSource());
						System.out.println("Topic:" + logGroup.GetTopic());
						for (LogItem log : logGroup.GetAllLogs()) {
							System.out.println("LogTime:" + log.GetTime());
							List<LogContent> contents = log.GetLogContents();
							for (LogContent content : contents) {
								System.out.println(content.GetKey() + ":"
										+ content.GetValue());
							}
						}
					}

					if (cursor.equals(next_cursor)) {
						break;
					}
					cursor = next_cursor;
				}
			}
		} catch (LogException e) {
			e.printStackTrace();
		}
	}

	public void ListShard() {
		try {
			ListShardResponse res = client.ListShard(project, logStore);
			System.out.println("RequestId:" + res.GetRequestId());
			Shard shard = res.GetShards().get(0);
			System.out.println("ShardId:" + shard.GetShardId());
		} catch (LogException e) {
			e.printStackTrace();
		}
	}
}

public class DataSample {
	public static void main(String[] args) {
		// ------------------------Data API------------------------
		ClientSample sample = new ClientSample();
		// ------------------------Shard------------------------
		sample.ListShard();

		sample.GetCursor();
		sample.BatchGetLog();
	}
}
