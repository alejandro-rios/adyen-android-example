package com.alejandrorios.adyenbasicexample.model;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import java.io.Serializable;

public class PaymentMethodsRequest implements Serializable {

	@Json(name = "amount")
	private Amount mAmount;

	@Json(name = "channel")
	private String mChannel = "Android";

	@Json(name = "token")
	private String mToken;

	@Json(name = "returnUrl")
	private String mReturnUrl;

	@Json(name = "countryCode")
	private String mCountryCode;

	@Json(name = "merchantAccount")
	private String mMerchantAccount;

	@Json(name = "shopperLocale")
	private String mShopperLocale;

	@Json(name = "shopperReference")
	private String mShopperReference;

	public static final class Builder {
		private final PaymentMethodsRequest mPaymentMethodsRequest;

		public Builder(@NonNull String merchantAccount, @NonNull String token, @NonNull String returnUrl, @NonNull Amount amount) {
			mPaymentMethodsRequest = new PaymentMethodsRequest();
			mPaymentMethodsRequest.mMerchantAccount = merchantAccount;
			mPaymentMethodsRequest.mToken = token;
			mPaymentMethodsRequest.mReturnUrl = returnUrl;
			mPaymentMethodsRequest.mAmount = amount;
		}

		@NonNull
		public PaymentMethodsRequest.Builder setShopperLocale(@NonNull String shopperLocale) {
			mPaymentMethodsRequest.mShopperLocale = shopperLocale;

			return this;
		}

		@NonNull
		public PaymentMethodsRequest.Builder setCountryCode(@NonNull String countryCode) {
			mPaymentMethodsRequest.mCountryCode = countryCode;

			return this;
		}

		@NonNull
		public Builder setShopperReference(@NonNull String shopperReference) {
			mPaymentMethodsRequest.mShopperReference = shopperReference;

			return this;
		}

		@NonNull
		public PaymentMethodsRequest build() {
			return mPaymentMethodsRequest;
		}
	}

	public static final class Amount implements Serializable {
		@Json(name = "value")
		private Long mValue;

		@Json(name = "currency")
		private String mCurrency;

		public Amount(@NonNull Long value, @NonNull String currency) {
			mValue = value;
			mCurrency = currency;
		}

		@NonNull
		public Long getValue() {
			return mValue;
		}

		@NonNull
		public String getCurrency() {
			return mCurrency;
		}
	}
}
