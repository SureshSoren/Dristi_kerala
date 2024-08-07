import { useIndividualView } from "./useIndividualView";
import utils from "../utils";
import { useGetPendingTask } from "./useGetPendingTask";
import { HomeService } from "./services";
const home = {
  useIndividualView,
  useGetPendingTask,
};

const Hooks = {
  home,
};

const Utils = {
  browser: {
    home: () => {},
  },
  home: {
    ...utils,
  },
};

export const CustomisedHooks = {
  Hooks,
  Utils,
  HomeService,
};
