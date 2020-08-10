package cn.lylf;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import java.util.Collection;
import java.util.Iterator;

public class HelloWorldAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        //获取当前操作的Project
        Project project = e.getProject();
        Collection<VirtualFile> virtualFiles = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project));
        Iterator<VirtualFile> iterator = virtualFiles.iterator();
        while (iterator.hasNext()){
            VirtualFile virtualFile = iterator.next();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            PsiJavaFile javaFile = (PsiJavaFile)psiFile;
            Document document = PsiDocumentManager.getInstance(project).getDocument(javaFile);

            PsiClass[] psiClasses = javaFile.getClasses();
            for (PsiClass psiClass: psiClasses){
                PsiMethod[] psiMethods = psiClass.getMethods();
                for (PsiMethod psiMethod: psiMethods){
                    String methodName = psiMethod.getName();
                    String methodText = psiMethod.getBody().getText();
                    int startLine = document.getLineNumber(psiMethod.getBody().getLBrace().getTextOffset()) + 1;
                    int endLine = document.getLineNumber(psiMethod.getBody().getRBrace().getTextOffset()) + 1;
                }
            }
        }

        //获取当前操作的类文件
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        //获取当前类文件的路径
        String classPath = psiFile.getVirtualFile().getPath();
        String title = "Hello World";
        String msg = project.getName() + "\n" + classPath;

        //显示对话框
        Messages.showMessageDialog(project, msg, title, Messages.getInformationIcon());
    }
}
