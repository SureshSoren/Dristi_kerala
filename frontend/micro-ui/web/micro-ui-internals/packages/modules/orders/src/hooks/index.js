import { useIndividualView } from "./useIndividualView";
import utils from "../utils";

import { ordersService } from "./services";
import useSearchOrdersService from "./orders/useSearchOrdersService";
import useESign from "./orders/useESign";

const orders = {
  useIndividualView,
  useSearchOrdersService,
  useESign,
};

const Hooks = {
  orders,
};

const Utils = {
  browser: {
    orders: () => {},
  },
  orders: {
    ...utils,
  },
};

export const CustomisedHooks = {
  Hooks,
  Utils,
  ordersService,
};
