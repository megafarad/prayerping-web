import { configureStore } from '@reduxjs/toolkit';
import userReducer from './userSlice';
import apiResponseReducer from './apiResponseSlice';
import webSocketStateReducer from './webSocketStateSlice';
import prayerFeedReducer from './prayerFeedSlice';
import prayerRequestReducer from './prayerRequestSlice';
import prayerReactionsReducer from './prayerReactionsSlice';
import prayerResponsesReducer from './prayerResponseSlice';
import prayerResponseReactionsReducer from './prayerResponseReactionsSlice';
import {webSocketMiddleware} from './webSocketMiddleware';
import {Socket} from '../util/Socket';

export default configureStore({
  reducer: {
    user: userReducer,
    apiResponse: apiResponseReducer,
    webSocketState: webSocketStateReducer,
    prayerFeed: prayerFeedReducer,
    prayerRequest: prayerRequestReducer,
    prayerReactions: prayerReactionsReducer,
    prayerResponses: prayerResponsesReducer,
    prayerResponseReactions: prayerResponseReactionsReducer
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware()
    .concat(webSocketMiddleware(new Socket()))
});
