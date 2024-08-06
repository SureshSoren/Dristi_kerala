import { useIndividualView } from "./useIndividualView";
import utils from "../utils";

import { ordersService } from "./services";
import { EpostService } from "./services";
import useSearchOrdersService from "./orders/useSearchOrdersService";
import useESign from "./orders/useESign";
import useUpdateEpost from "./Epost/UpdateEpost";
import useDocumentUpload from "./orders/useDocumentUpload";

const orders = {
  useIndividualView,
  useSearchOrdersService,
  useESign,
  useDocumentUpload,
};

const Epost = {
  useUpdateEpost,
};

const Hooks = {
  orders,
  Epost,
};

const Utils = {
  browser: {
    orders: () => {},
    Epost: () => {},
  },
  orders: {
    ...utils,
  },
  Epost: {
    ...utils,
  },
};

export const CustomisedHooks = {
  Hooks,
  Utils,
  ordersService,
  EpostService,
};
