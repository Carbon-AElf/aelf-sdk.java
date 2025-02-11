package io.aelf.internal;

import io.aelf.internal.global.TestParams;
import io.aelf.schemas.BlockDto;
import io.aelf.sdk.BlockChainSdkTest;
import io.aelf.internal.sdkv2.AElfClientV2;
import io.aelf.internal.sdkv2.AElfClientAsync;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

/**
 * Since the AsyncClient only provides internal-wrapped API,
 * and actually it doesn't contain additional code, This test
 * file will only test some of its APIS.
 * 
 * @see BlockChainSdkTest
 */
@SuppressWarnings("DataFlowIssue")
public class AElfClientAsyncTest {
    private AElfClientAsync client;
    String address = "";

    @Before
    public void init() {
        this.client = new AElfClientV2(TestParams.CLIENT_HTTP_URL);
        this.address = client.getAddressFromPrivateKey(TestParams.TEST_PRIVATE_KEY);
    }

    private void onFail(@Nonnull AsyncResult<Void> e) {
        System.out.println("Test Failed!" + e);
        throw new RuntimeException();
    }

    @Test
    public void getBlockHeightAsyncTest() {
        AsyncTestSingleLooper<Long> looper = new AsyncTestSingleLooper<>(height -> height.result > 0);
        client.getBlockHeightAsync(looper::setResult, this::onFail);
        looper.loop();
    }

    @Test
    public void getBlockByHashAsyncTest() throws Exception {
        long blockHeight = client.getBlockHeight();
        Assert.assertTrue(blockHeight > 0);
        BlockDto blockDto = client.getBlockByHeight(blockHeight);
        AsyncTestSingleLooper<BlockDto> looper = new AsyncTestSingleLooper<>(block -> block.result != null);
        client.getBlockByHashAsync(blockDto.getBlockHash(), looper::setResult, this::onFail);
        looper.loop();
    }

    @Test
    public void manyRequestsPressureTest() {
        AsyncTestLooper<Long> looper = new AsyncTestLooper<>(res -> res.result > 0,
                res -> res != null && !res.isOk(),60*1000);
        int size = 10;
        looper.setDeterminedSize(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            client.getBlockHeightAsync(res -> looper.putResultAtPosition(finalI, res), this::onFail);
        }
        looper.loop();
    }

}
