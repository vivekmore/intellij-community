package com.jetbrains.edu.coursecreator.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.TestActionEvent;
import com.jetbrains.edu.coursecreator.CCTestCase;
import com.jetbrains.edu.learning.courseFormat.AnswerPlaceholder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CCShowPreviewTest extends CCTestCase {

  public void testPreviewUnavailable() {
    VirtualFile file = configureByTaskFile("noplaceholders.txt");
    CCShowPreview action = new CCShowPreview();
    TestActionEvent e = getActionEvent(action, getPsiManager().findFile(file));
    action.beforeActionPerformedUpdate(e);
    assertTrue(e.getPresentation().isEnabled() && e.getPresentation().isVisible());
    try {
      action.actionPerformed(e);
      assertTrue("No message shown", false);
    } catch (RuntimeException ex) {
      assertEquals(CCShowPreview.NO_PREVIEW_MESSAGE, ex.getMessage());
    }
  }

  public void testOnePlaceholder() {
    doTest("test_before.txt", "test_after.txt");
  }

  public void testSeveralPlaceholders() {
    doTest("several_before.txt", "several_after.txt");
  }

  private void doTest(String beforeName, String afterName) {
    VirtualFile file = configureByTaskFile(beforeName);
    CCShowPreview action = new CCShowPreview();
    TestActionEvent e = getActionEvent(action, getPsiManager().findFile(file));
    action.beforeActionPerformedUpdate(e);
    assertTrue(e.getPresentation().isEnabled() && e.getPresentation().isVisible());
    action.actionPerformed(e);
    Editor editor = EditorFactory.getInstance().getAllEditors()[1];
    Pair<Document, List<AnswerPlaceholder>> pair = getPlaceholders(afterName);
    assertEquals("Files don't match", editor.getDocument().getText(), pair.getFirst().getText());
    for (AnswerPlaceholder placeholder : pair.getSecond()) {
      assertNotNull("No highlighter for placeholder", getHighlighter(editor.getMarkupModel(), placeholder));
    }
    EditorFactory.getInstance().releaseEditor(editor);
  }

  @Nullable
  private static RangeHighlighter getHighlighter(MarkupModel model, AnswerPlaceholder placeholder) {
    for (RangeHighlighter highlighter : model.getAllHighlighters()) {
      int endOffset = placeholder.getOffset() + placeholder.getRealLength();
      if (highlighter.getStartOffset() == placeholder.getOffset() && highlighter.getEndOffset() == endOffset) {
        return highlighter;
      }
    }
    return null;
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/actions/preview";
  }

  @Override
  protected boolean shouldContainTempFiles() {
    return false;
  }

  TestActionEvent getActionEvent(AnAction action, PsiFile psiFile) {
    MapDataContext context = new MapDataContext();
    context.put(CommonDataKeys.PSI_FILE, psiFile);
    context.put(CommonDataKeys.PROJECT, getProject());
    context.put(LangDataKeys.MODULE, myFixture.getModule());
    return new TestActionEvent(context, action);
  }
}
