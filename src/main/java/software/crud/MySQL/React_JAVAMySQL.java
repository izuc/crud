package software.crud.MySQL;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import software.crud.Models.*;
import software.crud.Utility.*;

import com.ibm.icu.text.PluralRules;
import com.google.common.base.CaseFormat;

public class React_JAVAMySQL {
	private List<Exception> exList;
	private Map<String, FinalQueryData> finalDataDic;

	private String templateFolder;
	private String templateFolderSeparator;
	private String destinationFolder;
	private String packageName;

	private ArrayList<CrudMessage> messages;
	private MySQLDBHelper mysqlDB;

	public React_JAVAMySQL(String packageName) {
		this.destinationFolder = System.getProperty("user.dir") + File.separator + packageName;
		this.packageName = packageName;
		this.exList = new ArrayList<>();
		this.templateFolder = "ReactTemplate";
		this.templateFolderSeparator = "\\\\";
		this.exList = new ArrayList<>();
		this.messages = new ArrayList<>();
	}

	public static class Inflector {
		private static LinkedList<Rule> singulars = new LinkedList<>();
		private static Set<String> uncountables = new HashSet<>();

		static {
			initialize(); // Call initialize statically
		}

		protected static class Rule {
			public final String expression;
			public final Pattern expressionPattern;
			public final String replacement;

			public Rule(String expression, String replacement) {
				this.expression = expression;
				this.replacement = replacement != null ? replacement : "";
				this.expressionPattern = Pattern.compile(this.expression, Pattern.CASE_INSENSITIVE);
			}

			public String apply(String input) {
				Matcher matcher = this.expressionPattern.matcher(input);
				if (!matcher.find())
					return null;
				return matcher.replaceAll(this.replacement);
			}
		}

		public static String depluralize(String word) {
			if (word == null)
				return null;
			String wordStr = word.trim();
			if (wordStr.isEmpty() || isUncountable(wordStr))
				return wordStr;

			for (Rule rule : singulars) {
				String result = rule.apply(wordStr);
				if (result != null)
					return result;
			}
			return wordStr;
		}

		private static boolean isUncountable(String word) {
			return uncountables.contains(word.toLowerCase());
		}

		private static void initialize() {
			// Rules for regular patterns
			addSingularRule("s$", "");
			addSingularRule("(ss)$", "$1"); // Keeps words ending in 'ss' unchanged like 'class' to 'class'
			addSingularRule("(n)ews$", "$1ews");
			addSingularRule("([ti])a$", "$1um");
			addSingularRule("([^aeiouy]|qu)ies$", "$1y"); // Changes 'cities' to 'city'
			addSingularRule("([^aeiouy])ies$", "$1y"); // Covers bases not covered by the above rule
			addSingularRule("([lr])ves$", "$1f"); // Changes 'wolves' to 'wolf'
			addSingularRule("([^f])ves$", "$1fe"); // Changes 'knives' to 'knife'
			addSingularRule("(hive)s$", "$1");
			addSingularRule("(tive)s$", "$1");
			addSingularRule("(^analy)ses$", "$1sis");
			addSingularRule("([^aeiouy]|qu)ies$", "$1y");
			addSingularRule("(s)eries$", "$1eries");
			addSingularRule("(m)ovies$", "$1ovie");
			addSingularRule("(x|ch|ss|sh)es$", "$1"); // Changes 'batches' to 'batch'
			addSingularRule("([m|l])ice$", "$1ouse");
			addSingularRule("(bus)es$", "$1");
			addSingularRule("(o)es$", "$1");
			addSingularRule("(shoe)s$", "$1");
			addSingularRule("(cris|ax|test)es$", "$1is");
			addSingularRule("(octop|vir)i$", "$1us");
			addSingularRule("(alias|status)es$", "$1");
			addSingularRule("^(ox)en", "$1");
			addSingularRule("(vert|ind)ices$", "$1ex");
			addSingularRule("(matr)ices$", "$1ix");
			addSingularRule("(quiz)zes$", "$1");

			// Special irregular forms
			addIrregular("person", "people");
			addIrregular("man", "men");
			addIrregular("child", "children");
			addIrregular("sex", "sexes");
			addIrregular("move", "moves");
			addIrregular("goose", "geese");
			addIrregular("alumnus", "alumni");

			// Handle uncountable nouns that should not be altered
			addUncountable("equipment", "information", "rice", "money", "species", "series", "fish", "sheep", "jeans",
					"police");
		}

		private static void addIrregular(String singular, String plural) {
			// Because the rules are processed in order, we must add these rules before
			// others
			String singularSuffix = singular.substring(1);
			String pluralSuffix = plural.substring(1);
			addSingularRule(pluralSuffix + "$", singularSuffix); // From plural to singular
		}

		private static void addSingularRule(String rule, String replacement) {
			singulars.addFirst(new Rule(rule, replacement));
		}

		private static void addUncountable(String... words) {
			for (String word : words) {
				if (word != null)
					uncountables.add(word.trim().toLowerCase());
			}
		}
	}

	public String getTemplateFolder() {
		return templateFolder;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public String getProjectName() {
		String[] packageParts = packageName.split("\\.");
		return packageParts[packageParts.length - 1];
	}

	public ArrayList<CrudMessage> getMessages() {
		return messages;
	}

	private void logMessage(String message, boolean isSuccess) {
		messages.add(new CrudMessage(message, isSuccess));
	}

	private String createPath(String filePathString, boolean isTemplate) {
		String separator = isTemplate ? templateFolderSeparator : File.separator;
		String path = isTemplate ? templateFolder : destinationFolder;
		String[] parts = filePathString.split(",");

		for (String part : parts) {
			if (part != null && !part.trim().isEmpty()) {
				path = path + separator + part.trim();
			}
		}

		if (!isTemplate) {
			File file = new File(path);
			if (!file.exists()) {
				File parentDir = file.getParentFile();
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs();
				}
			}
		}

		return path;
	}

	private String createDirectory() {
		String projectDirectory = destinationFolder;
		File directory = new File(projectDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return projectDirectory;
	}

	private void createModelFile() {
		for (Map.Entry<String, FinalQueryData> item : finalDataDic.entrySet()) {
			try {
				List<ColumnModel> columns = item.getValue().getSelectQueryData().getColumnList();
				String key = item.getKey();
				String ModelName = toModelName(key, true);
				String path = createPath(String.join(",", packageName.split("\\.")) + ",model," + ModelName + ".java",
						false);

				StringBuilder classContent = new StringBuilder();
				classContent.append("package ").append(packageName).append(".model;\n\n");
				classContent.append("import ").append(packageName).append(".storage.StorageName;\n\n");
				classContent.append("@StorageName(\"").append(key).append("\")\n");
				classContent.append("public class ").append(ModelName).append(" extends ExtendedModel {\n\n");

				for (ColumnModel column : columns) {
					String fieldType = Helper.getDataTypeJava(column.getTypeName());
					String fieldName = Helper.toCamelCase(column.getField());

					classContent.append("    private ").append(fieldType).append(" ").append(fieldName).append(";\n\n");

					classContent.append("    public ").append(fieldType).append(" get")
							.append(Helper.toPascalCase(fieldName)).append("() {\n");
					classContent.append("        return ").append(fieldName).append(";\n");
					classContent.append("    }\n\n");

					classContent.append("    public void set").append(Helper.toPascalCase(fieldName)).append("(")
							.append(fieldType).append(" ").append(fieldName).append(") {\n");
					classContent.append("        this.").append(fieldName).append(" = ")
							.append(fieldType.equals("String") ? fieldName + ".trim()" : fieldName).append(";\n");
					classContent.append("    }\n\n");
				}

				classContent.append("}\n");

				CopyDir.writeWithoutBOM(path, classContent.toString());

				logMessage("Model class for " + ModelName + " generated successfully!", true);
			} catch (Exception ex) {
				logMessage("Error generating model for " + item.getKey() + ": " + ex.getMessage(), false);
				ex.printStackTrace();
			}
		}
	}

	private void createResourceFile() {
		for (Map.Entry<String, FinalQueryData> item : finalDataDic.entrySet()) {
			try {
				String key = item.getKey();
				String ModelName = toModelName(key, true);
				String path = createPath(
						String.join(",", packageName.split("\\.")) + ",api,resource," + ModelName + "Resource.java",
						false);

				StringBuilder classContent = new StringBuilder();
				classContent.append("package ").append(packageName).append(".api.resource;\n\n");
				classContent.append("import jakarta.ws.rs.*;\n");
				classContent.append("import jakarta.ws.rs.core.MediaType;\n\n");
				classContent.append("import ").append(packageName).append(".api.ExtendedObjectResource;\n");
				classContent.append("import ").append(packageName).append(".model.").append(ModelName).append(";\n\n");

				classContent.append("@Path(\"").append(Helper.toCamelCase(key)).append("\")\n");
				classContent.append("@Produces(MediaType.APPLICATION_JSON)\n");
				classContent.append("@Consumes(MediaType.APPLICATION_JSON)\n");
				classContent.append("public class ").append(ModelName)
						.append("Resource extends ExtendedObjectResource<").append(ModelName).append("> {\n\n");

				classContent.append("    public ").append(ModelName).append("Resource() {\n");
				classContent.append("        super(").append(ModelName).append(".class);\n");
				classContent.append("    }\n\n");

				classContent.append("}\n");

				CopyDir.writeWithoutBOM(path, classContent.toString());

				logMessage("Resource class for " + ModelName + " generated successfully!", true);
			} catch (Exception ex) {
				logMessage("Error generating resource for " + item.getKey() + ": " + ex.getMessage(), false);
				ex.printStackTrace();
			}
		}
	}

	private void createStoreFile() {
		for (Map.Entry<String, FinalQueryData> entry : finalDataDic.entrySet()) {
			try {
				String tableName = entry.getKey();
				String ModelName = toModelName(tableName, false);
				String modelName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, ModelName);

				String path = createPath("src,store," + modelName + ".js", false);

				StringBuilder storeContent = new StringBuilder();
				storeContent.append("import { createSlice } from '@reduxjs/toolkit';\n\n");

				storeContent.append("const { reducer, actions } = createSlice({\n");
				storeContent.append("  name: '").append(modelName).append("',\n");
				storeContent.append("  initialState: { items: {} },\n");
				storeContent.append("  reducers: {\n");
				storeContent.append("    refresh(state, action) {\n");
				storeContent.append("      state.items = {};\n");
				storeContent.append("      action.payload.forEach((item) => state.items[item.id] = item);\n");
				storeContent.append("    },\n");
				storeContent.append("  },\n");
				storeContent.append("});\n\n");

				storeContent.append("export { actions as ").append(modelName).append("Actions };\n");
				storeContent.append("export { reducer as ").append(modelName).append("Reducer };\n");

				CopyDir.writeWithoutBOM(path, storeContent.toString());

				logMessage("Store file for " + modelName + " generated successfully!", true);
			} catch (Exception ex) {
				logMessage("Error generating store for " + entry.getKey() + ": " + ex.getMessage(), false);
				ex.printStackTrace();
			}
		}
	}

	private void createFormView() {
		for (Map.Entry<String, FinalQueryData> entry : finalDataDic.entrySet()) {
			try {
				String tableName = entry.getKey();
				String ModelName = toModelName(tableName, true);
				String modelName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, ModelName);
				List<ColumnModel> columns = entry.getValue().getSelectQueryData().getColumnList();

				String path = createPath("src,main,webapp," + ModelName + "FormView.js", false);

				StringBuilder formContent = new StringBuilder();
				formContent.append("import React, { useState } from 'react';\n");
				formContent.append(
						"import { TextField, Accordion, AccordionSummary, AccordionDetails, Typography } from '@mui/material';\n");
				formContent.append("import ExpandMoreIcon from '@mui/icons-material/ExpandMore';\n");
				formContent.append("import EditItemView from './components/EditItemView';\n");
				formContent.append("import EditAttributesAccordion from './components/EditAttributesAccordion';\n");
				formContent.append("import { useTranslation } from '../common/components/LocalizationProvider';\n");
				formContent.append("import SettingsMenu from './components/SettingsMenu';\n");
				formContent.append("import useSettingsStyles from './common/useSettingsStyles';\n\n");

				formContent.append("const ").append(ModelName).append("FormView = () => {\n");
				formContent.append("  const classes = useSettingsStyles();\n");
				formContent.append("  const t = useTranslation();\n\n");

				formContent.append("  const [item, setItem] = useState();\n\n");

				formContent.append("  const validate = () => item");
				for (ColumnModel column : columns) {
					formContent.append(" && item.").append(Helper.toCamelCase(column.getField()));
				}
				formContent.append(";\n\n");

				formContent.append("  return (\n");
				formContent.append("    <EditItemView\n");
				formContent.append("      endpoint=\"").append(modelName).append("\"\n");
				formContent.append("      item={item}\n");
				formContent.append("      setItem={setItem}\n");
				formContent.append("      validate={validate}\n");
				formContent.append("      menu={<SettingsMenu />}\n");
				formContent.append("      breadcrumbs={['settingsTitle', 'shared").append(ModelName).append("']}\n");
				formContent.append("    >\n");
				formContent.append("      {item && (\n");
				formContent.append("        <>\n");
				formContent.append("          <Accordion defaultExpanded>\n");
				formContent.append("            <AccordionSummary expandIcon={<ExpandMoreIcon />}>\n");
				formContent
						.append("              <Typography variant=\"subtitle1\">{t('sharedRequired')}</Typography>\n");
				formContent.append("            </AccordionSummary>\n");
				formContent.append("            <AccordionDetails className={classes.details}>\n");

				for (ColumnModel column : columns) {
					String fieldName = Helper.toCamelCase(column.getField());
					formContent.append("              <TextField\n");
					formContent.append("                value={item.").append(fieldName).append(" || ''}\n");
					formContent.append("                onChange={(event) => setItem({ ...item, ").append(fieldName)
							.append(": event.target.value })}\n");
					formContent.append("                label={t('").append(fieldName).append("')}\n");
					formContent.append("              />\n");
				}

				formContent.append("            </AccordionDetails>\n");
				formContent.append("          </Accordion>\n");
				formContent.append("          <EditAttributesAccordion\n");
				formContent.append("            attributes={item.attributes}\n");
				formContent.append("            setAttributes={(attributes) => setItem({ ...item, attributes })}\n");
				formContent.append("            definitions={{}}\n");
				formContent.append("          />\n");
				formContent.append("        </>\n");
				formContent.append("      )}\n");
				formContent.append("    </EditItemView>\n");
				formContent.append("  );\n");
				formContent.append("};\n\n");

				formContent.append("export default ").append(ModelName).append("FormView;\n");

				CopyDir.writeWithoutBOM(path, formContent.toString());

				logMessage("Form view for " + ModelName + " generated successfully!", true);
			} catch (Exception ex) {
				logMessage("Error generating form view for " + entry.getKey() + ": " + ex.getMessage(), false);
				ex.printStackTrace();
			}
		}
	}

	private void createReactPage() {
		for (Map.Entry<String, FinalQueryData> entry : finalDataDic.entrySet()) {
			try {
				String tableName = entry.getKey();
				String ModelName = toModelName(tableName, true);
				String modelName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, ModelName);

				String pluralModelName = toModelName(tableName, false);
				List<ColumnModel> columns = entry.getValue().getSelectQueryData().getColumnList();

				String path = createPath("src,main,webapp," + pluralModelName + "Page.js", false);

				StringBuilder pageContent = new StringBuilder();

				pageContent.append("import React, { useState } from 'react';\n");
				pageContent
						.append("import { Table, TableRow, TableCell, TableHead, TableBody } from '@mui/material';\n");
				pageContent.append("import { useEffectAsync } from '../reactHelper';\n");
				pageContent.append("import { useTranslation } from '../common/components/LocalizationProvider';\n");
				pageContent.append("import PageLayout from '../common/components/PageLayout';\n");
				pageContent.append("import SettingsMenu from './components/SettingsMenu';\n");
				pageContent.append("import CollectionFab from './components/CollectionFab';\n");
				pageContent.append("import CollectionActions from './components/CollectionActions';\n");
				pageContent.append("import TableShimmer from '../common/components/TableShimmer';\n");
				pageContent.append("import SearchHeader, { filterByKeyword } from './components/SearchHeader';\n");
				pageContent.append("import useSettingsStyles from './common/useSettingsStyles';\n\n");

				pageContent.append("const ").append(ModelName).append("Page = () => {\n");
				pageContent.append("  const classes = useSettingsStyles();\n");
				pageContent.append("  const t = useTranslation();\n\n");

				pageContent.append("  const [timestamp, setTimestamp] = useState(Date.now());\n");
				pageContent.append("  const [items, setItems] = useState([]);\n");
				pageContent.append("  const [searchKeyword, setSearchKeyword] = useState('');\n");
				pageContent.append("  const [loading, setLoading] = useState(false);\n\n");

				pageContent.append("  useEffectAsync(async () => {\n");
				pageContent.append("    setLoading(true);\n");
				pageContent.append("    try {\n");
				pageContent.append("      const response = await fetch('/api/").append(modelName).append("');\n");
				pageContent.append("      if (response.ok) {\n");
				pageContent.append("        setItems(await response.json());\n");
				pageContent.append("      } else {\n");
				pageContent.append("        throw Error(await response.text());\n");
				pageContent.append("      }\n");
				pageContent.append("    } finally {\n");
				pageContent.append("      setLoading(false);\n");
				pageContent.append("    }\n");
				pageContent.append("  }, [timestamp]);\n\n");

				pageContent.append("  return (\n");
				pageContent.append("    <PageLayout menu={<SettingsMenu />} breadcrumbs={['settingsTitle', 'shared")
						.append(ModelName).append("s']}>\n");
				pageContent.append("      <SearchHeader keyword={searchKeyword} setKeyword={setSearchKeyword} />\n");
				pageContent.append("      <Table className={classes.table}>\n");
				pageContent.append("        <TableHead>\n");
				pageContent.append("          <TableRow>\n");

				for (ColumnModel column : columns) {
					pageContent.append("            <TableCell>{t('").append(Helper.toCamelCase(column.getField()))
							.append("')}</TableCell>\n");
				}

				pageContent.append("            <TableCell className={classes.columnAction} />\n");
				pageContent.append("          </TableRow>\n");
				pageContent.append("        </TableHead>\n");
				pageContent.append("        <TableBody>\n");
				pageContent
						.append("          {!loading ? items.filter(filterByKeyword(searchKeyword)).map((item) => (\n");
				pageContent.append("            <TableRow key={item.id}>\n");

				for (ColumnModel column : columns) {
					pageContent.append("              <TableCell>{item.").append(Helper.toCamelCase(column.getField()))
							.append("}</TableCell>\n");
				}

				pageContent.append("              <TableCell className={classes.columnAction} padding=\"none\">\n");
				pageContent.append("                <CollectionActions itemId={item.id} editPath=\"/settings/")
						.append(pluralModelName).append("\" endpoint=\"").append(modelName)
						.append("\" setTimestamp={setTimestamp} />\n");
				pageContent.append("              </TableCell>\n");
				pageContent.append("            </TableRow>\n");
				pageContent.append("          )) : (<TableShimmer columns={").append(columns.size() + 1)
						.append("} endAction />)}\n");
				pageContent.append("        </TableBody>\n");
				pageContent.append("      </Table>\n");
				pageContent.append("      <CollectionFab editPath=\"/settings/").append(pluralModelName)
						.append("\" />\n");
				pageContent.append("    </PageLayout>\n");
				pageContent.append("  );\n");
				pageContent.append("};\n\n");

				pageContent.append("export default ").append(ModelName).append("Page;\n");

				CopyDir.writeWithoutBOM(path, pageContent.toString());

				logMessage("React page for " + pluralModelName + " generated successfully!", true);
			} catch (Exception ex) {
				logMessage("Error generating React page for " + entry.getKey() + ": " + ex.getMessage(), false);
				ex.printStackTrace();
			}
		}
	}

	private void updateRoutes() {
		try {
			StringBuilder routes = new StringBuilder();
			for (Map.Entry<String, FinalQueryData> entry : finalDataDic.entrySet()) {
				String tableName = entry.getKey();
				String singularModelName = toModelName(tableName, true);
				String pluralModelName = toModelName(tableName, false);

				String singularModelNameCamel = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, singularModelName);
				String pluralModelNameCamel = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, pluralModelName);

				routes.append("          <Route path=\"").append(pluralModelNameCamel).append("\" element={<")
						.append(singularModelName).append("Page />} />\n");
				routes.append("          <Route path=\"").append(singularModelNameCamel)
						.append("/edit/:id\" element={<").append(singularModelName).append("FormView />} />\n");
			}

			String path = createPath("src,main,webapp,routes.js", false);
			CopyDir.writeWithoutBOM(path, routes.toString());
		} catch (IOException ex) {
			logMessage("Error updating routes: " + ex.getMessage(), false);
			ex.printStackTrace();
		}
	}

	public List<Exception> createReactApp(CodeInput<FinalQueryData> reactInput) {
		createDirectory();
		finalDataDic = reactInput.getFinalDataDic();

		createModelFile();
		createResourceFile();
		createStoreFile();
		createFormView();
		createReactPage();
		updateRoutes();

		logMessage("----- React App Generated -----", false);
		logMessage("Please check the generated code at: " + destinationFolder, false);
		return exList;
	}

	public CodeInput<FinalQueryData> automator(String packageName, List<String> selectedTable, MySQLDBHelper mySQLDB)
			throws Exception {
		mysqlDB = mySQLDB;
		this.packageName = packageName;
		String text = createDirectory();
		logMessage("Project Folder Created: " + text, true);
		logMessage("Generating React Project...", true);
		logMessage("Copying Project File, Might take some time...", true);
		copyProject();
		logMessage("Finished Copying Project File", true);
		logMessage("Analyzing Database...", true);
		CodeInput<FinalQueryData> codeInput = new CodeInput<>();
		codeInput.setDestinationFolder(text);
		codeInput.setFinalDataDic(new HashMap<>());

		for (String item : selectedTable) {
			try {
				logMessage("Processing for Table => " + item, true);
				InsertUpdateQueryData insertUpdateQueryData = mysqlDB.getInsertUpdateQueryData(item);
				FinalQueryData finalQueryData = mysqlDB.buildLaravelQuery(item);
				finalQueryData.setInsertUpdateQueryData(insertUpdateQueryData);
				codeInput.getFinalDataDic().put(item, finalQueryData);
			} catch (Exception ex) {
				logMessage("Exception on table " + item + " - " + ex.getMessage(), false);
				StringBuilder sw = new StringBuilder();
				for (StackTraceElement ste : ex.getStackTrace()) {
					sw.append(ste.toString()).append("\n");
				}
				String exceptionAsString = sw.toString();
				logMessage(exceptionAsString, false);
			}
		}

		return codeInput;
	}

	private void copyProject() {
		String sourceDirectory = getTemplateFolder();
		File sourceDir = new File(sourceDirectory);
		if (sourceDir.exists() && sourceDir.isDirectory()) {
			String targetDirectory = getDestinationFolder();
			File targetDir = new File(targetDirectory);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			try {
				CopyDir.copyAll(sourceDir, targetDir, getProjectName(), "ReactProjectName");
				logMessage("Project files copied successfully!", true);
			} catch (Exception e) {
				logMessage("Error occurred while copying project files: " + e.getMessage(), false);
				e.printStackTrace();
			}
		} else {
			logMessage("Source directory does not exist or is not a directory: " + sourceDirectory, false);
		}
	}

	public static String pluralize(String word) {
		PluralRules pluralRules = PluralRules.forLocale(Locale.ENGLISH);
		String pluralForm = pluralRules.select(2);
		return word + (pluralForm.equals("one") ? "" : "s");
	}

	public static String depluralize(String word) {
		return Inflector.depluralize(word);
	}

	public static String toModelName(String tableName, boolean depluralize) {
		String camelCase = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName);
		return depluralize ? Inflector.depluralize(camelCase) : camelCase;
	}

	public static String toTableName(String modelName) {
		String underscore = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modelName);
		return pluralize(underscore);
	}
}