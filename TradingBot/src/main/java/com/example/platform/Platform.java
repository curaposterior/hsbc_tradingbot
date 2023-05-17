package com.example.platform;

import com.example.model.rest.*;

public interface Platform {

    PortfolioResponse portfolio();

    SubmitOrderResponse submit(SubmitOrderRequest order);

    HistoryResponse history(HistoryRequest historyRequest);

    InstrumentsResponse instruments();

    SubmittedResponse submitted();

    ProcessedResponse processed();
}
