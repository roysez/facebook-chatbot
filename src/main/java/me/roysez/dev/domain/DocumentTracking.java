package me.roysez.dev.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class DocumentTracking {

   @JsonProperty("StatusCode")
    private String statusCode;

    @JsonProperty("AfterpaymentOnGoodsCost")
    private String afterpaymentOnGoodsCost;

    @JsonProperty("DocumentWeight")
    private String documentWeight;

    @JsonProperty("LastTransactionDateTimeGM")
    private String lastTransactionDateTimeGM;

    @JsonProperty("WarehouseRecipientInternetAddressRef")
    private String warehouseRecipientInternetAddressRef;

    @JsonProperty("ServiceType")
    private String serviceType;

    @JsonProperty("CheckWeight")
    private String checkWeight;

    @JsonProperty("RedeliveryPaymentCardRef")
    private String redeliveryPaymentCardRef;

    @JsonProperty("LastTransactionStatusGM")
    private String lastTransactionStatusGM;

    @JsonProperty("LastCreatedOnTheBasisNumber")
    private String lastCreatedOnTheBasisNumber;

    @JsonProperty("RedeliveryPaymentCardDescription")
    private String redeliveryPaymentCardDescription;

    @JsonProperty("LastCreatedOnTheBasisDocumentType")
    private String lastCreatedOnTheBasisDocumentType;

    @JsonProperty("ScheduledDeliveryDate")
    private String scheduledDeliveryDate;

    @JsonProperty("CargoDescriptionString")
    private String cargoDescriptionString;

    @JsonProperty("LastAmountTransferGM")
    private String lastAmountTransferGM;

    @JsonProperty("OwnerDocumentType")
    private String ownerDocumentType;

    @JsonProperty("CounterpartyType")
    private String sounterpartyType;

    @JsonProperty("DocumentCost")
    private String documentCost;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("DateCreated")
    private String dateCreated;

    @JsonProperty("DateScan")
    private String dateScan;

    @JsonProperty("PaymentStatusDate")
    private String paymentStatusDate;

    @JsonProperty("PaymentMethod")
    private String paymentMethod;

    @JsonProperty("PaymentStatus")
    private String paymentStatus;

    @JsonProperty("CargoType")
    private String cargoType;

    @JsonProperty("PayerType")
    private String payerType;

    @JsonProperty("CitySender")
    private String citySender;

    @JsonProperty("RecipientFullName")
    private String recipientFullName;

    @JsonProperty("CityRecipient")
    private String cityRecipient;

    @JsonProperty("MarketplacePartnerToken")
    private String marketplacePartnerToken;

    @JsonProperty("LastCreatedOnTheBasisPayerType")
    private String lastCreatedOnTheBasisPayerType;

    @JsonProperty("RedeliveryNum")
    private String redeliveryNum;

    @JsonProperty("WarehouseRecipient")
    private String warehouseRecipient;

    @JsonProperty("CounterpartySenderType")
    private String counterpartySenderType;

    @JsonProperty("RecipientDateTime")
    private String recipientDateTime;

    @JsonProperty("RecipientAddress")
    private String recipientAddress;

    @JsonProperty("OwnerDocumentNumber")
    private String ownerDocumentNumber;

    @JsonProperty("RecipientWarehouseTypeRef")
    private String recipientWarehouseTypeRef;

    @JsonProperty("SumBeforeCheckWeight")
    private String sumBeforeCheckWeight;

    @JsonProperty("LastCreatedOnTheBasisDateTime")
    private String lastCreatedOnTheBasisDateTime;

    @JsonProperty("AmountToPay")
    private String amountToPay;

    @JsonProperty("RefEW")
    private String refEW;

    @JsonProperty("RedeliveryPayer")
    private String redeliveryPayer;

    @JsonProperty("WarehouseRecipientNumber")
    private String warehouseRecipientNumber;

    @JsonProperty("UndeliveryReasonsSubtypeDescription")
    private String undeliveryReasonsSubtypeDescription;

    @JsonProperty("AnnouncedPrice")
    private String announcedPrice;

    @JsonProperty("ClientBarcode")
    private String clientBarcode;

    @JsonProperty("CounterpartyRecipientDescription")
    private String counterpartyRecipientDescription;

    @JsonProperty("Redelivery")
    private String redelivery;

    @JsonProperty("LastAmountReceivedCommissionGM")
    private String lastAmountReceivedCommissionGM;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("RedeliverySum")
    private String redeliverySum;

    @JsonProperty("SenderAddress")
    private String senderAddress;

    @JsonProperty("AmountPaid")
    private String amountPaid;

    @JsonProperty("CounterpartySenderDescription")
    private String counterpartySenderDescription;

}
