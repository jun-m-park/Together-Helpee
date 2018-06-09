package com.example.junmp.togetherhelpee.domain.device;

import android.util.Log;
import com.example.junmp.togetherhelpee.common.util.network.RetrofitBuilder;

import java.io.IOException;

public class DeviceService {
    private DeviceRepository deviceRepository =  RetrofitBuilder.builder().create(DeviceRepository.class);

    public void save(String deviceId , String pushToken) {
        try {
            Log.d("Id" , deviceId);
            Log.d("Token" , pushToken);
            deviceRepository.save(deviceId , pushToken).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
