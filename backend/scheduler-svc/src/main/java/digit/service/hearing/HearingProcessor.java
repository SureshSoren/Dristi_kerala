package digit.service.hearing;


import digit.web.models.hearing.Hearing;
import digit.web.models.hearing.HearingRequest;
import digit.web.models.hearing.PresidedBy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HearingProcessor {


    public void processCreateHearingRequest(HearingRequest hearingRequest) {

        Hearing hearing = hearingRequest.getHearing();
        Long startTime = hearing.getStartTime();
        PresidedBy presidedBy = hearing.getPresidedBy();

        List<String> judgeID = presidedBy.getJudgeID();
        String courtID = presidedBy.getCourtID();

        // todo: attach start time and end time for particular day

        //todo: for now we will consider for one judge only ,enhancement is there for n judge in v2
    }
}
