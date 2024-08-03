package digit.channel;

import digit.web.models.ChannelMessage;
import digit.web.models.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EPostChannel implements ExternalChannel{

    @Override
    public ChannelMessage sendSummons(TaskRequest request) {
        return null;
    }
}
