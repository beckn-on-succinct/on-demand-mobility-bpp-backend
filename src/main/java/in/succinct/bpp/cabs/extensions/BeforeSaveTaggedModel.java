package in.succinct.bpp.cabs.extensions;

import com.venky.core.collections.SequenceSet;
import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.plugins.background.core.Task;
import com.venky.swf.plugins.background.core.TaskManager;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.tag.Tag;
import in.succinct.bpp.cabs.db.model.tag.Tagable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class BeforeSaveTaggedModel<T extends Tagable & Model> extends BeforeModelSaveExtension<T> {

    @Override
    public void beforeSave(T taggedModel) {
        if (!ObjectUtil.isVoid(taggedModel.getTags()) &&
                taggedModel.getRawRecord().isFieldDirty("TAGS")) {
            StringTokenizer tokenizer = new StringTokenizer(taggedModel.getTags(), ",");
            Set<String> tags = new TreeSet<>();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                tags.add(token);
            }
            StringBuilder sTag = new StringBuilder();
            for (String tag : tags) {
                if (sTag.length() > 0) {
                    sTag.append(",");
                }
                sTag.append(tag);
            }
            taggedModel.setTags(sTag.toString());// Sort Tags alphabetically

            List<Tag> tagList = new Select().from(Tag.class).where(new Expression(ModelReflector.instance(Tag.class).getPool(), "NAME", Operator.IN, tags.toArray())).execute();
            if (tagList.size() < tags.size()) {
                List<String> existingTags = new SequenceSet<>();
                for (Tag tag : tagList) {
                    existingTags.add(tag.getName());
                }
                tags.removeAll(existingTags);
                List<Task> tasks = new ArrayList<>();
                for (String tag : tags) {
                    tasks.add(new CreateTagTask(getModelClass(this).getSimpleName(),tag));
                }
                TaskManager.instance().executeAsync(tasks, false);
            }
        }
    }
    public static class CreateTagTask implements Task {
        String tag;
        String taggedModelName;
        public CreateTagTask(String taggedModelName,String tag){
            this.tag = tag;
            this.taggedModelName = taggedModelName;
        }
        public CreateTagTask(){

        }

        @Override
        public void execute() {
            Select select = new Select().from(Tag.class);
            Expression expression = new Expression(select.getPool(), Conjunction.AND).
                    add(new Expression(select.getPool(),"NAME",Operator.EQ,tag)).
                    add(new Expression(select.getPool(),"TAGGED_MODEL_NAME", Operator.EQ, taggedModelName));
            List<Tag> tags = select.where(expression).execute();
            if (tags.isEmpty()){
                Tag tag  = Database.getTable(Tag.class).newRecord();
                tag.setName(this.tag.trim());
                tag.setTaggedModelName(taggedModelName);
                tag = Database.getTable(Tag.class).getRefreshed(tag);
                tag.save();
            }
        }
    }
}
