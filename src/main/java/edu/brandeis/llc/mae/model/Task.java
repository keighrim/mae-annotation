/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.llc.mae.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by krim on 12/27/2015.
 */

@DatabaseTable(tableName = DBSchema.TAB_TASK)
public class Task implements ModelI {

    @DatabaseField(id = true, columnName = DBSchema.TAB_TASK_COL_NAME)
    private String name;

    @DatabaseField(columnName = DBSchema.TAB_TASK_COL_TEXT)
    private String primaryText;

    @DatabaseField(columnName = DBSchema.TAB_TASK_COL_TASKFILE)
    private String taskFileName;

    @DatabaseField(columnName = DBSchema.TAB_TASK_COL_ANNFILE)
    private String annotationFileName;

    public Task() {
    }

    public Task(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public void setPrimaryText(String primaryText) {
        this.primaryText = primaryText;
    }

    public String getTaskFileName() {
        return taskFileName;
    }

    public void setTaskFileName(String taskFileName) {
        this.taskFileName = taskFileName;
    }

    public String getAnnotationFileName() {
        return annotationFileName;
    }

    public void setAnnotationFileName(String annotationFileName) {
        this.annotationFileName = annotationFileName;
    }

    public boolean isTaskLoaded() {
        return getTaskFileName() != null;
    }

    public boolean isAnnotationLoaded() {
        return getAnnotationFileName() != null;
    }

    public boolean isPrimaryTextLoaded() {
        return getPrimaryText() != null;
    }

    @Override
    public String getId() {
        return getName();
    }
}
