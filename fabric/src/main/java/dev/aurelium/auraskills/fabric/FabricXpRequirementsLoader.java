package dev.aurelium.auraskills.fabric;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.config.ConfigurateLoader;
import dev.aurelium.auraskills.common.level.XpRequirements;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class FabricXpRequirementsLoader {

    private final AuraSkillsPlugin plugin;
    private final XpRequirements requirements;

    FabricXpRequirementsLoader(AuraSkillsPlugin plugin, XpRequirements requirements) {
        this.plugin = plugin;
        this.requirements = requirements;
    }

    void load() {
        ConfigurateLoader loader = new ConfigurateLoader(plugin, TypeSerializerCollection.builder().build());
        String fileName = "xp_requirements.yml";
        try {
            loader.generateUserFile(fileName);

            ConfigurationNode config = loader.loadUserFile(fileName);

            ConfigurationNode defaultConfig = config.node("default");
            if (!defaultConfig.virtual()) {
                try {
                    requirements.setDefaultXpRequirements(loadSection(defaultConfig));
                } catch (EvaluationException | ParseException e) {
                    plugin.logger().warn("Failed to evaluate default XP requirements expression");
                    e.printStackTrace();
                }
            }

            for (Skill skill : plugin.getSkillManager().getSkillValues()) {
                requirements.removeSkillXpRequirements(skill);

                ConfigurationNode skillNode = config.node("skills").node(skill.name().toLowerCase());
                if (skillNode.virtual()) {
                    skillNode = config.node("skills").node(skill.getId().toString());
                }
                if (!skillNode.virtual()) {
                    try {
                        requirements.setSkillXpRequirements(skill, loadSection(skillNode));
                    } catch (EvaluationException | ParseException e) {
                        plugin.logger().warn("Failed to evaluate XP requirement expression for skill " + skill);
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            plugin.logger().warn("Failed to load " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Integer> loadSection(ConfigurationNode config) throws SerializationException, EvaluationException, ParseException {
        List<Integer> list = new ArrayList<>();
        int highestMaxLevel = plugin.config().getHighestMaxLevel();

        if (!config.node("values").virtual()) {
            List<String> values = config.node("values").getList(String.class);
            Objects.requireNonNull(values);

            for (int i = 0; i < highestMaxLevel; i++) {
                if (i < values.size()) {
                    list.add(Integer.parseInt(values.get(i)));
                } else {
                    list.add(Integer.parseInt(values.get(values.size() - 1)));
                }
            }
        } else {
            Expression expression = getXpExpression(config);
            for (int i = 0; i < highestMaxLevel; i++) {
                expression.with("level", BigDecimal.valueOf(i + 2));
                list.add((int) Math.round(expression.evaluate().getNumberValue().doubleValue()));
            }
        }

        return list;
    }

    private Expression getXpExpression(ConfigurationNode config) {
        String expressionString = config.node("expression").getString();
        Objects.requireNonNull(expressionString);

        Expression expression = new Expression(expressionString);
        for (Object variableObj : config.childrenMap().keySet()) {
            String variable = (String) variableObj;

            if (variable.equals("expression")) continue;

            double variableValue = config.node(variable).getDouble();
            expression.with(variable, BigDecimal.valueOf(variableValue));
        }
        return expression;
    }
}
