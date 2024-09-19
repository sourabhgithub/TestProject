import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.Job;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private LoadReferenceDataTasklet loadReferenceDataTasklet;

    @Autowired
    private ProcessReconExceptionsTasklet processReconExceptionsTasklet;

    @Autowired
    private CreateReconExceptionsTasklet createReconExceptionsTasklet;

    @Autowired
    private PromotionListener promotionListener;

    @Bean
    public Job cashExceptionsProcessJob() {
        return jobBuilderFactory.get("cashExceptionsProcessJob")
                .start(loadRefDataStep())
                .next(dtbReconExceptionsStep())
                .build();
    }

    @Bean
    public Step loadRefDataStep() {
        return stepBuilderFactory.get("loadRefDataStep")
                .tasklet(loadReferenceDataTasklet)
                .listener(promotionListener)
                .build();
    }

    @Bean
    public Step dtbReconExceptionsStep() {
        return stepBuilderFactory.get("dtbReconExceptionsStep")
                .tasklet(processReconExceptionsTasklet)
                .build();
    }

    @Bean
    public Step createReconExceptionsStep() {
        return stepBuilderFactory.get("createReconExceptionsStep")
                .tasklet(createReconExceptionsTasklet)
                .build();
    }
}
