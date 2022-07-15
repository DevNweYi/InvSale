package com.bosictsolution.invsale.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private static Retrofit retrofit=null;

    public static ApiInterface getClient(){  // this is manual dependency injection
        if(retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl("http://localhost/InventoryWebService/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        ApiInterface api=retrofit.create(ApiInterface.class);
        return api;
    }
}
