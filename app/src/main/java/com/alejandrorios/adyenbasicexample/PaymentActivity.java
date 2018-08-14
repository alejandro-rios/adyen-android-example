package com.alejandrorios.adyenbasicexample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.RedirectDetails;
import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.card.CardValidator;
import com.adyen.checkout.core.card.internal.CardValidatorImpl;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.handler.RedirectHandler;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;

import java.util.List;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

	private PaymentMethod mPaymentMethod;
	private PaymentMethodDetails mPaymentMethodDetails;
	private PaymentHandler mPaymentHandler;
	private Button mButtonPay;
	private EditText mCreditCardNo;
	private EditText mCreditCardHolderName;
	private EditText mCreditCardExpDate;
	private EditText mCreditCardCvc;

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.button_make_pay) {

			if(cardValidations(mCreditCardHolderName.getText().toString(),
					mCreditCardNo.getText().toString(), mCreditCardExpDate.getText().toString(),
					mCreditCardCvc.getText().toString())){
				Toast.makeText(this, "VALID", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "INVALID", Toast.LENGTH_SHORT).show();
			}


			// Can use this to create my CC data?
			CardDetails cardDetails = new CardDetails.Builder()
					.setHolderName(mCreditCardHolderName.getText().toString())
					.setEncryptedCardNumber(mCreditCardNo.getText().toString())
					.setEncryptedSecurityCode(mCreditCardCvc.getText().toString())
					.setEncryptedExpiryMonth("10")
					.setEncryptedExpiryYear("20")
					.build();
			// Searching what's next here.....

			Card cardd = new Card.Builder()
					.setNumber(mCreditCardNo.getText().toString())
					.setExpiryDate(10, 20)
					.setSecurityCode(mCreditCardNo.getText().toString())
					.build();


		}
	}

	private boolean cardValidations(String HolderName, String Number, String ExpDate, String Cvc) {
		boolean valid = true;
		CardValidatorImpl validator = new CardValidatorImpl('-', '/');
//		CardType cardType = new CardType();

//		if(validator.validateHolderName(HolderName).equals(CardValidator.Validity.INVALID)){
//			valid = false;
//		}

		if(validator.validateNumber(Number).equals(CardValidator.Validity.INVALID)){
			valid = false;
		}

		if(validator.validateExpiryDate(ExpDate).equals(CardValidator.Validity.INVALID)){
			valid = false;
		}

//		if(validator.validateSecurityCode(Cvc, true, ).equals(CardValidator.Validity.INVALID)){
//			valid = false;
//		}

		return valid;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		mCreditCardNo = findViewById(R.id.credit_card_no);
		mCreditCardHolderName = findViewById(R.id.credit_card_holder_name);
		mCreditCardExpDate = findViewById(R.id.credit_card_exp_date);
		mCreditCardCvc = findViewById(R.id.credit_card_cvc);

		mButtonPay = findViewById(R.id.button_make_pay);
		mButtonPay.setOnClickListener(this);

		Bundle bundle = getIntent().getExtras();
		try {
			PaymentReference paymentRef = bundle.getParcelable("EXTRA_PAYMENT_REFERENCE");
			List<PaymentMethod> paymentMethod = bundle.getParcelableArrayList("EXTRA_PAYMENT_METHOD");        // Works ok
			PaymentExplore(paymentRef);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void PaymentExplore(PaymentReference paymentReference) {
		mPaymentHandler = paymentReference.getPaymentHandler(this);

		// Observe data
		mPaymentHandler.getNetworkingStateObservable().observe(this, new Observer<NetworkingState>() {
			@Override
			public void onChanged(@NonNull NetworkingState networkingState) {
				// TODO: Handle NetworkingState.
			}
		});
		mPaymentHandler.getPaymentSessionObservable().observe(this, new Observer<PaymentSession>() {
			@Override
			public void onChanged(@NonNull PaymentSession paymentSession) {
				// TODO: Handle PaymentSession change, i.e. refresh your list of PaymentMethods.
			}
		});
		mPaymentHandler.getPaymentResultObservable().observe(this, new Observer<PaymentResult>() {
			@Override
			public void onChanged(@NonNull PaymentResult paymentResult) {
				// TODO: Handle PaymentResult.
			}
		});

		// Handle data
		mPaymentHandler.setRedirectHandler(this, new RedirectHandler() {
			@Override
			public void onRedirectRequired(@NonNull RedirectDetails redirectDetails) {
				// TODO: Handle RedirectDetails.
			}
		});
		mPaymentHandler.setAdditionalDetailsHandler(this, new AdditionalDetailsHandler() {
			@Override
			public void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails) {
				// TODO: Handle AdditionalDetails.
			}
		});
		mPaymentHandler.setErrorHandler(this, new ErrorHandler() {
			@Override
			public void onError(@NonNull CheckoutException error) {
				// TODO: Handle CheckoutException.
			}
		});
	}

}
