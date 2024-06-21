package com.chr1s.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.FLOW_GRADE_QPS;

@Component
public class SentinelRuleConfig implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        ArrayList<FlowRule> rules = new ArrayList<>();
        FlowRule createOrderRule = new FlowRule();
        createOrderRule.setResource("create_short-link");
        createOrderRule.setGrade(FLOW_GRADE_QPS);
        createOrderRule.setCount(1);
        rules.add(createOrderRule);
        FlowRuleManager.loadRules(rules);
    }
}
