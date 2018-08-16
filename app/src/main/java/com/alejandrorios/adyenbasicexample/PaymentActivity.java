package com.alejandrorios.adyenbasicexample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adyen.checkout.base.internal.Objects;
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
import com.adyen.checkout.core.card.Cards;
import com.adyen.checkout.core.card.EncryptedCard;
import com.adyen.checkout.core.card.EncryptionException;
import com.adyen.checkout.core.card.internal.CardValidatorImpl;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.handler.RedirectHandler;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.card.CardHandler;
import com.adyen.checkout.ui.internal.common.util.PaymentMethodUtil;
import com.adyen.checkout.ui.internal.common.util.PhoneNumberUtil;
import com.adyen.checkout.ui.internal.common.util.TextViewUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentActivity extends AppCompatActivity {

	private List<PaymentMethod> mAllowedPaymentMethods;
	private PaymentMethod mPaymentMethod;
	private PaymentMethodDetails mPaymentMethodDetails;
	private PaymentHandler mPaymentHandler;
	private PaymentReference mpaymentRef;

	@BindView(R.id.credit_card_no)
	EditText mCreditCardNo;

	@BindView(R.id.credit_card_holder_name)
	EditText mCreditCardHolderName;

	@BindView(R.id.credit_card_exp_date)
	EditText mCreditCardExpDate;

	@BindView(R.id.credit_card_cvc)
	EditText mCreditCardCvc;

	@BindView(R.id.button_make_pay)
	Button mButtonPay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment);

		ButterKnife.bind(this);

		Bundle bundle = getIntent().getExtras();
		try {
			mpaymentRef = bundle.getParcelable("EXTRA_PAYMENT_REFERENCE");
			mAllowedPaymentMethods = bundle.getParcelableArrayList("EXTRA_PAYMENT_METHOD");        // Works ok
			PaymentExplore(mpaymentRef);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClick(R.id.button_make_pay)
	public void onPayClicked() {

		if (cardValidations(mCreditCardHolderName.getText().toString(),
				mCreditCardNo.getText().toString(), mCreditCardExpDate.getText().toString(),
				mCreditCardCvc.getText().toString())) {
			Toast.makeText(this, "VALID", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "INVALID", Toast.LENGTH_SHORT).show();
		}


		// Can use this to create my CC data?
//		CardDetails cardDetails = buildCardDetails();

		new CardDetails.Builder()
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

	private CardDetails buildCardDetails() throws EncryptionException {

		CardValidator.HolderNameValidationResult holderNameValidationResult = getHolderNameValidationResult();
//		PhoneNumberUtil.ValidationResult phoneNumberValidationResult = getPhoneNumberValidationResult();
		Card card = parseCardFromInput();

		PaymentSession paymentSession = mpaymentRef;

		if (paymentSession == null) {
			return null;
		}

		Date generationTime = paymentSession.getGenerationTime();
		String publicKey = Objects.requireNonNull(paymentSession.getPublicKey(), CardHandler.ERROR_MESSAGE_PUBLIC_KEY_NULL);
		EncryptedCard encryptedCard;

		try {
			//noinspection ConstantConditions, validated by isValidCardDetails()
			encryptedCard = Cards.ENCRYPTOR.encryptFields(card, generationTime, publicKey).call();
		} catch (EncryptionException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Failed to encrypt card.", e);
		}

		return new CardDetails.Builder()
				.setHolderName(holderNameValidationResult.getHolderName())
				.setEncryptedCardNumber(encryptedCard.getEncryptedNumber())
				.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth())
				.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear())
				.setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode())
//				.setPhoneNumber(phoneNumberValidationResult.getPhoneNumber())
//				.setStoreDetails(getStoreDetails())
//				.setInstallments(getInstallments())
				.build();

	}

//	private void validateHolderNameEditText() {
//		CardValidator.HolderNameValidationResult validationResult = getHolderNameValidationResult();
//
//		if (validationResult.getValidity() == CardValidator.Validity.VALID) {
//			TextViewUtil.setDefaultTextColor(mCreditCardHolderName);
//		} else if (mCreditCardHolderName.hasFocus() && validationResult.getValidity() == CardValidator.Validity.PARTIAL) {
//			TextViewUtil.setDefaultTextColor(mCreditCardHolderName);
//		} else {
//			TextViewUtil.setErrorTextColor(mCreditCardHolderName);
//		}
//	}

	@NonNull
	private CardValidator.HolderNameValidationResult getHolderNameValidationResult() {
		String holderName = mCreditCardHolderName.getText().toString();
		PaymentMethodUtil.Requirement holderNameRequirement = PaymentMethodUtil
				.getRequirementForInputDetail(CardDetails.KEY_HOLDER_NAME, mAllowedPaymentMethods);

		return Cards.VALIDATOR.validateHolderName(holderName, holderNameRequirement == PaymentMethodUtil.Requirement.REQUIRED);
	}

	@NonNull
	private CardValidator.SecurityCodeValidationResult getSecurityCodeValidationResult() {
		String securityCode = mCreditCardCvc.getText().toString();
		boolean isRequired;
		CardType cardType;

		if (mAllowedPaymentMethods.size() == 1) {
			PaymentMethod paymentMethod = mAllowedPaymentMethods.get(0);
			isRequired = PaymentMethodUtil
					.getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, paymentMethod) == PaymentMethodUtil.Requirement.REQUIRED;
			cardType = CardType.forTxVariantProvider(paymentMethod);
		} else {
			List<CardType> estimatedCardTypes = getCardTypesToDisplay();

			if (estimatedCardTypes.size() == 1 && estimatedCardTypes.get(0) == CardType.AMERICAN_EXPRESS) {
				isRequired = true;
				cardType = CardType.AMERICAN_EXPRESS;
			} else {
				isRequired = PaymentMethodUtil.getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, mAllowedPaymentMethods)
						== PaymentMethodUtil.Requirement.REQUIRED;
				cardType = null;
			}
		}

		return Cards.VALIDATOR.validateSecurityCode(securityCode, isRequired, cardType);
	}

	private boolean cardValidations(String HolderName, String Number, String ExpDate, String Cvc) {
		boolean valid = true;
		CardValidatorImpl validator = new CardValidatorImpl('-', '/');
//		CardType cardType = new CardType();

//		if(validator.validateHolderName(HolderName).equals(CardValidator.Validity.INVALID)){
//			valid = false;
//		}

		if (validator.validateNumber(Number).equals(CardValidator.Validity.INVALID)) {
			valid = false;
		}

		if (validator.validateExpiryDate(ExpDate).equals(CardValidator.Validity.INVALID)) {
			valid = false;
		}

//		if(validator.validateSecurityCode(Cvc, true, ).equals(CardValidator.Validity.INVALID)){
//			valid = false;
//		}

		return valid;
	}

	@Nullable
	private Card parseCardFromInput() {
		String cardNumber = mCreditCardNo.getText().toString();
		CardValidator.NumberValidationResult numberValidationResult = Cards.VALIDATOR.validateNumber(cardNumber);

		if (numberValidationResult.getValidity() != CardValidator.Validity.VALID) {
			return null;
		}

		String expiryDate = mCreditCardExpDate.getText().toString();
		CardValidator.ExpiryDateValidationResult expiryDateValidationResult = Cards.VALIDATOR.validateExpiryDate(expiryDate);

		if (expiryDateValidationResult.getValidity() != CardValidator.Validity.VALID) {
			return null;
		}

		CardValidator.SecurityCodeValidationResult securityCodeValidationResult = getSecurityCodeValidationResult();

		if (securityCodeValidationResult.getValidity() != CardValidator.Validity.VALID) {
			return null;
		}

		//noinspection ConstantConditions
		return new Card.Builder()
				.setNumber(numberValidationResult.getNumber())
				.setExpiryDate(expiryDateValidationResult.getExpiryMonth(), expiryDateValidationResult.getExpiryYear())
				.setSecurityCode(securityCodeValidationResult.getSecurityCode())
				.build();
	}

	@NonNull
	private List<CardType> getCardTypesToDisplay() {
		List<CardType> cardTypesToDisplay = new ArrayList<>();
		String cardNumber = Cards.VALIDATOR.validateNumber(mCreditCardNo.getText().toString()).getNumber();

		if (cardNumber != null) {
			for (PaymentMethod allowedPaymentMethod : mAllowedPaymentMethods) {
				CardType cardType = CardType.forTxVariantProvider(allowedPaymentMethod);

				if (cardType != null && cardType.isEstimateFor(cardNumber)) {
					cardTypesToDisplay.add(cardType);
				}
			}
		}

		return cardTypesToDisplay;
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
