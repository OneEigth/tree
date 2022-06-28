import entity.Tree;

import javax.persistence.*;
import java.util.List;
import java.util.Scanner;

public class Application {

    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("main");

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        EntityManager manager = FACTORY.createEntityManager();
        TypedQuery<Tree> treeTypedQuery = manager.createQuery(
                "select t from Tree t", Tree.class
        );
        List<Tree> treeList = treeTypedQuery.getResultList();
        String level = "";
        for (Tree tree : treeList) {
            for (int i = 1; i <= tree.getLevel(); i++) {
                level = level + "- ";
            }
            System.out.println(level + tree.getName() + "[" + tree.getId() + "]");
            level = "";
        }
        System.out.println("______________________");
        System.out.println("- Создание товара [1]");
        System.out.println("- Перемещение товара [2]");
        System.out.println("- Удаление товара [3]");
        System.out.println("-Выберите действие: ");
        String actionNum = IN.nextLine();
        switch (actionNum) {
            case "1" -> create();
            case "2" -> movement();
            case "3" -> delete();
            default -> System.out.println("Такого действия не существует");
        }
    }

    private static void create() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            System.out.print("Куда добавить: ");
            String numCategory = IN.nextLine();

            System.out.print("Название: ");
            String nameCategory = IN.nextLine();

            if (!numCategory.equals("0")) {
                Tree tree = manager.find(Tree.class, Integer.parseInt(numCategory));
                Query expansionLeftKey = manager.createQuery(
                        "update Tree t SET t.left=t.left+2 where t.left>:leftKey"
                );
                expansionLeftKey.setParameter("leftKey", tree.getRight());
                expansionLeftKey.executeUpdate();

                Query expansionRightKey = manager.createQuery(
                        "update Tree t SET t.right=t.right+2 where t.right>=:rightKey"
                );
                expansionRightKey.setParameter("rightKey", tree.getRight());
                expansionRightKey.executeUpdate();

                Tree newElement = new Tree();
                newElement.setName(nameCategory);
                newElement.setLeft(tree.getRight());
                newElement.setRight(tree.getRight() + 1);
                newElement.setLevel(tree.getLevel() + 1);
                manager.persist(newElement);
            } else {
                TypedQuery<Integer> maxRightKeyTypedQuery = manager.createQuery(
                        "select  max (t.right)  from Tree t", Integer.class
                );
                Integer maxRightKey = maxRightKeyTypedQuery.getSingleResult();
                Tree newElement = new Tree();
                newElement.setName(nameCategory);
                newElement.setLeft(maxRightKey + 1);
                newElement.setRight(maxRightKey + 2);
                newElement.setLevel(Integer.parseInt(numCategory));
                manager.persist(newElement);
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }

    private static void movement() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            System.out.print("Введите идентификатор перемещаемой категории: ");
            String numCategoryIN = IN.nextLine();

            System.out.print("Введите идентификатор новой родительской категории: ");
            String newNumCategoryIN = IN.nextLine();

            if (!newNumCategoryIN.equals("0")) {
                Tree tree = manager.find(Tree.class, Integer.parseInt(numCategoryIN));
                // where t.left >= ?1 and t.right <= ?2

                Query hidingKeys = manager.createQuery(
                        "update Tree t set  t.left=t.left*-1, t.right=t.right*-1  where t.left >= ?1 and t.right <= ?2"
                );
                hidingKeys.setParameter(1, tree.getLeft());
                hidingKeys.setParameter(2, tree.getRight());
                hidingKeys.executeUpdate();

                Query updateLeftKeys = manager.createQuery(
                        "update Tree t SET t.left=t.left-?1 where t.left>:rightKey"
                );
                updateLeftKeys.setParameter("rightKey", tree.getRight());
                updateLeftKeys.setParameter(1, tree.getRight() - tree.getLeft() + 1);
                updateLeftKeys.executeUpdate();

                Query updateRightKeys = manager.createQuery(
                        "update Tree t SET t.right=t.right-?2 where t.right>:rightKey"
                );
                updateRightKeys.setParameter("rightKey", tree.getRight());
                updateRightKeys.setParameter(2, tree.getRight() - tree.getLeft() + 1);
                updateRightKeys.executeUpdate();

                Tree newParent = manager.find(Tree.class, Integer.parseInt(newNumCategoryIN));
                manager.refresh(newParent);

                Query expansionRightKeys = manager.createQuery(
                        "update Tree t set t.right=t.right+?1 where t.right>=?2"
                );
                expansionRightKeys.setParameter(1, (tree.getRight() - tree.getLeft()) + 1);
                expansionRightKeys.setParameter(2, newParent.getRight());
                expansionRightKeys.executeUpdate();

                Query expansionLeftKey = manager.createQuery(
                        "update Tree t set t.left=t.left+?1 where t.left>=?2"
                );
                expansionLeftKey.setParameter(1, (tree.getRight() - tree.getLeft()) + 1);
                expansionLeftKey.setParameter(2, newParent.getRight());
                expansionLeftKey.executeUpdate();
                manager.refresh(newParent);

                Query expansionRightKey = manager.createQuery(
                        "update Tree t set t.left=0-(t.left)+?1,t.right=0-(t.right)+?1,t.level=t.level-?2+?3+1 where t.left<0"
                );
                expansionRightKey.setParameter(1, (newParent.getRight() - tree.getRight()) - 1);
                expansionRightKey.setParameter(2, tree.getLevel());
                expansionRightKey.setParameter(3, newParent.getLevel());
                expansionRightKey.executeUpdate();
            } else {
                Tree tree = manager.find(Tree.class, Integer.parseInt(numCategoryIN));
                Query hidingLeftKey = manager.createQuery(
                        "update Tree t set  t.left=t.left*-1 where t.left between ?1 and ?2"
                );
                hidingLeftKey.setParameter(1, tree.getLeft());
                hidingLeftKey.setParameter(2, tree.getRight());
                hidingLeftKey.executeUpdate();

                Query hidingRightKey = manager.createQuery(
                        "update Tree t set  t.right=t.right*-1 where t.right between ?1 and ?2"
                );
                hidingRightKey.setParameter(1, tree.getLeft());
                hidingRightKey.setParameter(2, tree.getRight());
                hidingRightKey.executeUpdate();

                Query updateLeftKey = manager.createQuery(
                        "update Tree t SET t.left=t.left-?1 where t.left>:rightKey"
                );
                updateLeftKey.setParameter("rightKey", tree.getRight());
                updateLeftKey.setParameter(1, tree.getRight() - tree.getLeft() + 1);
                updateLeftKey.executeUpdate();

                Query updateRightKey = manager.createQuery(
                        "update Tree t SET t.right=t.right-?2 where t.right>:rightKey"
                );
                updateRightKey.setParameter("rightKey", tree.getRight());
                updateRightKey.setParameter(2, tree.getRight() - tree.getLeft() + 1);
                updateRightKey.executeUpdate();

                TypedQuery<Integer> typedQuery = manager.createQuery(
                        "select max(t.right) from Tree t", Integer.class
                );
                Integer treeMaxRight = typedQuery.getSingleResult();

                Query managerQuery = manager.createQuery(
                        "update Tree t set t.left=0-(t.left)+?1, t.right=0-(t.right)+?1, t.level=t.level-?2 where t.left<0"
                );
                managerQuery.setParameter(1, (((treeMaxRight + 1) + (tree.getRight() - tree.getLeft())) - tree.getRight()));
                managerQuery.setParameter(2, tree.getLevel());
                managerQuery.executeUpdate();
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }

    private static void delete() {
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            System.out.print("номер категории: ");
            String numCategory = IN.nextLine();

            Tree tree = manager.find(Tree.class, Integer.parseInt(numCategory));
            Query deleteQuery = manager.createQuery(
                    "delete from Tree t where t.left between ?1 and ?2"
            );
            deleteQuery.setParameter(1, tree.getLeft());
            deleteQuery.setParameter(2, tree.getRight());
            deleteQuery.executeUpdate();

            Query updateLeftKey = manager.createQuery(
                    "update Tree t SET t.left=t.left-?1 where t.left>:rightKey"
            );
            updateLeftKey.setParameter("rightKey", tree.getRight());
            updateLeftKey.setParameter(1, tree.getRight() - tree.getLeft() + 1);
            updateLeftKey.executeUpdate();

            Query updateRightKey = manager.createQuery(
                    "update Tree t SET t.right=t.right-?2 where t.right>:rightKey"
            );

            updateRightKey.setParameter("rightKey", tree.getRight());
            updateRightKey.setParameter(2, tree.getRight() - tree.getLeft() + 1);
            updateRightKey.executeUpdate();
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }
}
